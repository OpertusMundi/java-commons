package eu.opertusmundi.common.service;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.AccountAssetEntity;
import eu.opertusmundi.common.domain.AccountSubscriptionEntity;
import eu.opertusmundi.common.domain.AccountSubscriptionSkuEntity;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInItemEntity;
import eu.opertusmundi.common.domain.PayInOrderItemEntity;
import eu.opertusmundi.common.model.account.EnumAssetSource;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.order.ConsumerOrderDto;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.OrderConfirmCommandDto;
import eu.opertusmundi.common.model.order.OrderDeliveryCommandDto;
import eu.opertusmundi.common.model.order.OrderException;
import eu.opertusmundi.common.model.order.OrderMessageCode;
import eu.opertusmundi.common.model.order.OrderShippingCommandDto;
import eu.opertusmundi.common.model.order.ProviderOrderDto;
import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.model.pricing.CallPrePaidPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import eu.opertusmundi.common.model.pricing.FixedPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.PrePaidTierDto;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;
import eu.opertusmundi.common.model.pricing.RowPrePaidPricingModelCommandDto;
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.repository.AccountAssetRepository;
import eu.opertusmundi.common.repository.AccountSubscriptionRepository;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.repository.PayInRepository;
import eu.opertusmundi.common.util.BpmEngineUtils;
import eu.opertusmundi.common.util.BpmInstanceVariablesBuilder;
import feign.FeignException;

@Service
@Transactional
public class DefaultOrderFulfillmentService implements OrderFulfillmentService {

    private static final String WORKFLOW_PROCESS_PAYIN = "workflow-process-payin";

    private static final String MESSAGE_UPDATE_PAYIN_STATUS = "payin-updated-message";

    private static final String MESSAGE_PROVIDER_SEND_ORDER = "provider-send-order-message";

    private static final String MESSAGE_CONSUMER_RECEIVED_ORDER = "consumer-order-received-message";

    private static final Logger logger = LoggerFactory.getLogger(DefaultOrderFulfillmentService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PayInRepository payInRepository;

    @Autowired
    private AccountAssetRepository accountAssetRepository;

    @Autowired
    private AccountSubscriptionRepository accountSubscriptionRepository;

    @Autowired
    private BpmEngineUtils bpmEngine;

    @Autowired
    private CatalogueService catalogueService;

    @Override
    public ProviderOrderDto acceptOrder(OrderConfirmCommandDto command) throws OrderException {
        return this.confirmOrder(command.getPublisherKey(), command.getOrderKey(), true, null);
    }

    @Override
    public ProviderOrderDto rejectOrder(OrderConfirmCommandDto command) throws OrderException {
        return this.confirmOrder(command.getPublisherKey(), command.getOrderKey(), false, command.getReason());
    }

    @Transactional
    private ProviderOrderDto confirmOrder(UUID publisherKey, UUID orderKey, boolean accepted, String rejectReason) throws OrderException {
        try {
            ProviderOrderDto order;
            if (accepted) {
                order = this.orderRepository.acceptOrder(publisherKey, orderKey);
            } else {
                order = this.orderRepository.rejectOrder(publisherKey, orderKey, rejectReason);
            }

            return order;
        } catch (final OrderException ex) {
            throw ex;
        } catch (final FeignException fex) {
            logger.error("Operation has failed", fex);

            throw new OrderException(OrderMessageCode.BPM_SERVICE, "Operation on BPM server failed", fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw new OrderException(OrderMessageCode.ERROR, "An unknown error has occurred", ex);
        }
    }

    /**
     * Initializes a workflow instance to process the referenced PayIn
     *
     * The operation may fail because of (a) a network error, (b) BPM engine
     * service error or (c) database command error. The operation is retried for
     * at most 3 times, with a maximum latency due to attempt delays of 9
     * seconds.
     */
    @Override
    @Retryable(include = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, maxDelay = 3000))
    public String startOrderWorkflow(UUID payInKey, String payInId, EnumTransactionStatus payInStatus) {
        try {
            final PayInEntity payIn = payInRepository.findOneEntityByKey(payInKey).orElse(null);
            Assert.isTrue(payIn.getItems().size() == 1, "Expected a single pay in item");
            final EnumPaymentItemType type = payIn.getItems().get(0).getType();
            Assert.isTrue(type == EnumPaymentItemType.ORDER, "Expected an order pay in item");
            final PayInOrderItemEntity payInItem = (PayInOrderItemEntity) payIn.getItems().get(0);

            if (!StringUtils.isBlank(payIn.getProcessInstance())) {
                // Workflow instance already exists
                return payIn.getProcessInstance();
            }

            final ProcessInstanceDto instance = this.bpmEngine.findInstance(payInKey.toString());
            if (instance == null) {
                // Set business key
                final String businessKey= payInKey.toString();

                // Set variables
                final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                    .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), "")
                    .variableAsString("payInKey", payInKey.toString())
                    .variableAsString("payInId", payInId)
                    .variableAsString("payInStatus", payInStatus.toString())
                    .variableAsString("deliveryMethod", payInItem.getOrder().getDeliveryMethod().toString())
                    .build();



                this.bpmEngine.startProcessDefinitionByKey(WORKFLOW_PROCESS_PAYIN, businessKey, variables);
            }

            payInRepository.setPayInWorkflowInstance(payIn.getId(), instance.getDefinitionId(), instance.getId());

            return instance.getId();
        } catch(final Exception ex) {
            logger.error(
                "Failed to start workflow instance [workflow={}, businessKey={}, ex={}]",
                WORKFLOW_PROCESS_PAYIN, payInKey, ex.getMessage()
            );
        }

        return null;
    }

    @Override
    @Retryable(include = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, maxDelay = 3000))
    public void sendPayInStatusUpdateMessage(UUID payInKey, EnumTransactionStatus status) {
        try {
            final PayInEntity                   payIn       = payInRepository.findOneEntityByKey(payInKey).orElse(null);
            final String                        businessKey = payIn.getKey().toString();
            final Map<String, VariableValueDto> variables   = BpmInstanceVariablesBuilder.builder()
                .variableAsString("payInStatus", status.toString())
                .build();

            this.bpmEngine.correlateMessage(businessKey, MESSAGE_UPDATE_PAYIN_STATUS, variables);
        } catch (final FeignException fex) {
            if (fex.status() == HttpStatus.BAD_REQUEST.value()) {
                // No process definition or execution matches the parameters.
                //
                // This error may occur if a web hook message is received before
                // the workflow instance is started. This may happen after a
                // successful direct card PayIn without 3-D Secure
                // validation.
            } else {
                logger.error(
                    "Failed to send message [workflow={}, businessKey={}, message={}, ex={}]",
                    WORKFLOW_PROCESS_PAYIN, payInKey, MESSAGE_UPDATE_PAYIN_STATUS, fex.getMessage()
                );
                throw fex;
            }
        } catch(final Exception ex) {
            logger.error(
                "Failed to send message [workflow={}, businessKey={}, message={}, ex={}]",
                WORKFLOW_PROCESS_PAYIN, payInKey, MESSAGE_UPDATE_PAYIN_STATUS, ex.getMessage()
            );
            throw ex;
        }
    }

    @Override
    @Transactional
    public ProviderOrderDto sendOrderByProvider(OrderShippingCommandDto command) throws OrderException {
        try {
            Assert.notNull(command.getPublisherKey(), "Expected a non-null publisher key");
            Assert.notNull(command.getOrderKey(), "Expected a non-null order key");

            final PayInEntity payIn = payInRepository.findOneByOrderKey(command.getOrderKey()).orElse(null);
            Assert.isTrue(payIn.getItems().size() == 1, "Expected a single pay in item");
            Assert.isTrue(
                payIn.getItems().get(0).getProvider().getKey().equals(command.getPublisherKey()),
                "Expected pay in publisher to match command"
            );

            final ProviderOrderDto result = this.orderRepository.sendOrderByProvider(command);

            final String                        businessKey = payIn.getKey().toString();
            final Map<String, VariableValueDto> variables   = BpmInstanceVariablesBuilder.builder().build();

            this.bpmEngine.correlateMessage(businessKey, MESSAGE_PROVIDER_SEND_ORDER, variables);

            return result;
        } catch (final OrderException ex) {
            throw ex;
        } catch (final FeignException fex) {
            logger.error("Operation has failed", fex);

            throw new OrderException(OrderMessageCode.BPM_SERVICE, "Operation on BPM server failed", fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw new OrderException(OrderMessageCode.ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    @Transactional
    public ConsumerOrderDto receiveOrderByConsumer(OrderDeliveryCommandDto command) throws OrderException {
        try {
            Assert.notNull(command.getConsumerKey(), "Expected a non-null consumer key");
            Assert.notNull(command.getOrderKey(), "Expected a non-null order key");

            final PayInEntity payIn = payInRepository.findOneByOrderKey(command.getOrderKey()).orElse(null);
            Assert.isTrue(payIn.getItems().size() == 1, "Expected a single pay in item");
            Assert.isTrue(
                payIn.getConsumer().getKey().equals(command.getConsumerKey()),
                "Expected pay in consumer to match command"
            );

            final ConsumerOrderDto result = this.orderRepository.receiveOrderByConsumer(command);

            final String                        businessKey = payIn.getKey().toString();
            final Map<String, VariableValueDto> variables   = BpmInstanceVariablesBuilder.builder().build();

            this.bpmEngine.correlateMessage(businessKey, MESSAGE_CONSUMER_RECEIVED_ORDER, variables);

            return result;
        } catch (final OrderException ex) {
            throw ex;
        } catch (final FeignException fex) {
            logger.error("Operation has failed", fex);

            throw new OrderException(OrderMessageCode.BPM_SERVICE, "Operation on BPM server failed", fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw new OrderException(OrderMessageCode.ERROR, "An unknown error has occurred", ex);
        }
    }

    /**
     * Update user profile with purchased assets and subscriptions
     *
     * This operation is invoked by workflow instances and will be retried by
     * the BPM engine
     */
    @Override
    public void updateConsumer(UUID payInKey) throws Exception {
        final PayInEntity payIn = payInRepository.findOneEntityByKey(payInKey).orElse(null);

        // Update account profile
        for (final PayInItemEntity item : payIn.getItems()) {
            switch (item.getType()) {
                case ORDER :
                    final PayInOrderItemEntity orderItem = (PayInOrderItemEntity) item;

                    this.registerOrderItem(payIn, (PayInOrderItemEntity) item);

                    // Complete order
                    this.orderRepository.setStatus(orderItem.getOrder().getKey(), EnumOrderStatus.SUCCEEDED);
                    break;

                default :
                    throw new PaymentException(PaymentMessageCode.PAYIN_ITEM_TYPE_NOT_SUPPORTED, String.format(
                        "PayIn item type not supported [payIn=%s, index=%d, type=%s]",
                        payInKey, item.getIndex(), item.getType()
                    ));
            }
        }
    }

    private void registerOrderItem(PayInEntity payIn, PayInOrderItemEntity payInItem) {
        final OrderEntity             order     = payInItem.getOrder();
        final OrderItemEntity         orderItem = order.getItems().get(0);
        final CatalogueItemDetailsDto asset      = this.catalogueService.findOne(null, orderItem.getAssetId(), null, false);

        switch(asset.getType()) {
            case VECTOR :
            case RASTER :
                this.registerAsset(payIn, payInItem, asset);
                break;

            case SERVICE :
                this.registerSubscription(payIn, payInItem, asset);
                break;

            default :
                throw new PaymentException(PaymentMessageCode.PAYIN_ASSET_TYPE_NOT_SUPPORTED, String.format(
                    "PayIn asset type not supported [payIn=%s, index=%d, type=%s]",
                    payIn.getKey(), orderItem.getIndex(), asset.getType(
                )));
        }
    }

    private void registerAsset(PayInEntity payIn, PayInOrderItemEntity payInItem, CatalogueItemDetailsDto asset) {
        // An order contains only a single item
        final UUID                     userKey      = payIn.getConsumer().getKey();
        final OrderEntity              order        = payInItem.getOrder();
        final OrderItemEntity          orderItem    = order.getItems().get(0);
        final EffectivePricingModelDto pricingModel = orderItem.getPricingModel();

        // Check if the order item is already registered
        final AccountAssetEntity ownedAsset = accountAssetRepository.findAllByUserKeyAndAssetId(userKey, orderItem.getAssetId()).stream()
            .filter(a -> a.getOrder().getId() == order.getId())
            .findFirst().orElse(null);
        // TODO: Update existing asset i.e. update update years of free updates
        if (ownedAsset != null) {
            return;
        }

        // Register asset to consumer's account
        final AccountAssetEntity reg = new AccountAssetEntity();

        reg.setAddedOn(ZonedDateTime.now());
        reg.setAsset(orderItem.getAssetId());
        reg.setConsumer(payIn.getConsumer());
        reg.setOrder(order);
        reg.setPurchasedOn(payIn.getExecutedOn());
        reg.setSource(EnumAssetSource.PURCHASE);
        reg.setUpdateEligibility(payIn.getExecutedOn());
        reg.setUpdateInterval(0);

        if (pricingModel.getModel().getType() == EnumPricingModel.FIXED) {
            final FixedPricingModelCommandDto fixedPricingModel = (FixedPricingModelCommandDto) pricingModel.getModel();
            final Integer                     yearsOfUpdates    = fixedPricingModel.getYearsOfUpdates();

            if (fixedPricingModel.getYearsOfUpdates() > 0) {
                reg.setUpdateEligibility(payIn.getExecutedOn().plusYears(yearsOfUpdates));
                reg.setUpdateInterval(yearsOfUpdates);
            }
        }

        this.accountAssetRepository.save(reg);
    }

    private void registerSubscription(PayInEntity payIn, PayInOrderItemEntity payInItem, CatalogueItemDetailsDto asset) {
        // An order contains only a single item
        final UUID                     userKey      = payIn.getConsumer().getKey();
        final OrderEntity              order        = payInItem.getOrder();
        final OrderItemEntity          orderItem    = order.getItems().get(0);
        final EffectivePricingModelDto pricingModel = orderItem.getPricingModel();
        final ZonedDateTime            now          = ZonedDateTime.now();

        // Check if a subscription is already active
        final AccountSubscriptionEntity activeSubscription = accountSubscriptionRepository.findAllByUserKeyAndServiceId(userKey, orderItem.getAssetId()).stream()
            .filter(a -> a.getOrder().getId() == order.getId())
            .findFirst().orElse(null);
        final boolean renewal = activeSubscription != null;

        if(renewal) {
            activeSubscription.setUpdatedOn(now);
        }
        // Create/Update subscription for consumer account
        final AccountSubscriptionEntity sub = new AccountSubscriptionEntity();

        sub.setAddedOn(now);
        sub.setConsumer(payIn.getConsumer());
        sub.setProvider(orderItem.getProvider());
        sub.setOrder(order);
        sub.setService(orderItem.getAssetId());
        sub.setUpdatedOn(now);
        sub.setSegment(asset.getTopicCategory().stream().findFirst().orElse(null));
        sub.setSource(renewal ? EnumAssetSource.UPDATE : EnumAssetSource.PURCHASE);

        // Register call/rows SKUs
        final QuotationParametersDto params = pricingModel.getParameters();
        AccountSubscriptionSkuEntity sku    = null;

        switch (pricingModel.getModel().getType()) {
            case PER_CALL_WITH_PREPAID :
                final CallPrePaidPricingModelCommandDto prePaidCallModel = (CallPrePaidPricingModelCommandDto) pricingModel.getModel();
                final PrePaidTierDto callTier = prePaidCallModel.getPrePaidTiers().get(params.getPrePaidTier());

                sku = new AccountSubscriptionSkuEntity();
                sku.setPurchasedCalls(callTier.getCount());

                break;

            case PER_ROW_WITH_PREPAID :
                final RowPrePaidPricingModelCommandDto prePaidRowModel = (RowPrePaidPricingModelCommandDto) pricingModel.getModel();
                final PrePaidTierDto rowTier = prePaidRowModel.getPrePaidTiers().get(params.getPrePaidTier());

                sku = new AccountSubscriptionSkuEntity();
                sku.setPurchasedCalls(rowTier.getCount());

                break;

            default :
                // No action is required
                break;
        }
        if (sku != null) {
            sku.setOrder(order);
            sku.setSubscription(sub);
            sku.setUsedCalls(0);
            sku.setUsedRows(0);

            sub.getSkus().add(sku);
        }

        this.accountSubscriptionRepository.save(sub);
    }

}
