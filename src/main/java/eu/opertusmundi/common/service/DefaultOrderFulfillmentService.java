package eu.opertusmundi.common.service;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.domain.AccountAssetEntity;
import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.AccountSubscriptionEntity;
import eu.opertusmundi.common.domain.AccountSubscriptionSkuEntity;
import eu.opertusmundi.common.domain.CardDirectPayInEntity;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInItemEntity;
import eu.opertusmundi.common.domain.PayInOrderItemEntity;
import eu.opertusmundi.common.feign.client.EmailServiceFeignClient;
import eu.opertusmundi.common.feign.client.MessageServiceFeignClient;
import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.account.EnumAssetSource;
import eu.opertusmundi.common.model.account.EnumSubscriptionStatus;
import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.asset.BundleAssetResourceDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import eu.opertusmundi.common.model.email.EmailAddressDto;
import eu.opertusmundi.common.model.email.EnumMailType;
import eu.opertusmundi.common.model.email.MailMessageCode;
import eu.opertusmundi.common.model.email.MessageDto;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.message.EnumNotificationType;
import eu.opertusmundi.common.model.message.server.ServerNotificationCommandDto;
import eu.opertusmundi.common.model.order.AcceptOrderContractCommand;
import eu.opertusmundi.common.model.order.ConsumerOrderDto;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.HelpdeskOrderDto;
import eu.opertusmundi.common.model.order.OrderConfirmCommandDto;
import eu.opertusmundi.common.model.order.OrderDeliveryCommand;
import eu.opertusmundi.common.model.order.OrderException;
import eu.opertusmundi.common.model.order.OrderMessageCode;
import eu.opertusmundi.common.model.order.OrderShippingCommandDto;
import eu.opertusmundi.common.model.order.ProviderOrderDto;
import eu.opertusmundi.common.model.order.UploadOrderContractCommand;
import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
import eu.opertusmundi.common.model.payment.EnumPaymentMethod;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskOrderPayInItemDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import eu.opertusmundi.common.model.pricing.FixedPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.PerCallPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.PerCallQuotationParametersDto;
import eu.opertusmundi.common.model.pricing.PerRowPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.PerRowQuotationParametersDto;
import eu.opertusmundi.common.model.pricing.PrePaidTierDto;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.model.workflow.EnumWorkflow;
import eu.opertusmundi.common.repository.AccountAssetRepository;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.AccountSubscriptionRepository;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.repository.PayInRepository;
import eu.opertusmundi.common.service.contract.ContractFileManager;
import eu.opertusmundi.common.service.integration.DataProviderManager;
import eu.opertusmundi.common.service.messaging.MailMessageHelper;
import eu.opertusmundi.common.service.messaging.MailModelBuilder;
import eu.opertusmundi.common.service.messaging.NotificationMessageHelper;
import eu.opertusmundi.common.util.BpmEngineUtils;
import eu.opertusmundi.common.util.BpmInstanceVariablesBuilder;
import feign.FeignException;

@Service
@Transactional
public class DefaultOrderFulfillmentService implements OrderFulfillmentService {

    private static final String MESSAGE_UPDATE_PAYIN_STATUS = "payin-updated-message";

    private static final String MESSAGE_PROVIDER_SEND_ORDER = "provider-send-order-message";

    private static final String MESSAGE_CONSUMER_RECEIVED_ORDER = "consumer-order-received-message";

    private static final Set<PosixFilePermission> DEFAULT_DIRECTORY_PERMISSIONS = PosixFilePermissions.fromString("rwxrwxr-x");

    private static final Logger logger = LoggerFactory.getLogger(DefaultOrderFulfillmentService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PayInRepository payInRepository;

    @Autowired
    private AccountAssetRepository accountAssetRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountSubscriptionRepository accountSubscriptionRepository;

    @Autowired
    private MailMessageHelper messageHelper;

    @Autowired
    private ObjectProvider<EmailServiceFeignClient> mailClient;

    @Autowired
    private ObjectProvider<MessageServiceFeignClient> messageClient;

    @Autowired
    private NotificationMessageHelper notificationMessageBuilder;

    @Autowired
    private BpmEngineUtils bpmEngine;

    @Autowired
    private CatalogueService catalogueService;

    @Autowired
    private DataProviderManager dataProviderManager;

    @Autowired
    private ContractFileManager contractFileManager;

    @Autowired
    private StatisticsService statisticsService;

    @Override
    @Transactional
    public ProviderOrderDto acceptOrderByProvider(OrderConfirmCommandDto command) throws OrderException {
        return this.confirmOrder(command.getPublisherKey(), command.getOrderKey(), true, null);
    }

    @Override
    @Transactional
    public ProviderOrderDto rejectOrderByProvider(OrderConfirmCommandDto command) throws OrderException {
        return this.confirmOrder(command.getPublisherKey(), command.getOrderKey(), false, command.getReason());
    }

    private ProviderOrderDto confirmOrder(UUID publisherKey, UUID orderKey, boolean accepted, String rejectReason) throws OrderException {
        try {
            ProviderOrderDto          order;
            final UUID                consumerKey = this.orderRepository.findByKey(orderKey).get().getConsumer().getKey();
            final Map<String, Object> variables   = new HashMap<>();

            if (accepted) {
                order = this.orderRepository.acceptOrderByProvider(publisherKey, orderKey);

                this.sendOrderStatusByMail(EnumMailType.CONSUMER_PURCHASE_APPROVED, consumerKey, orderKey);

                final EnumNotificationType notificationType = EnumNotificationType.PURCHASE_APPROVED;
                final String               idempotentKey    = orderKey.toString() + "::" + notificationType.toString();
                variables.put("orderKey", orderKey.toString());

                this.sendOrderStatusByNotification(notificationType, consumerKey, variables, idempotentKey);

                if (this.orderRepository.findByKey(orderKey).get().getItems().get(0).getContractType() == EnumContractType.UPLOADED_CONTRACT) {
                    this.sendOrderStatusByMail(EnumMailType.SUPPLIER_CONTRACT_TO_BE_FILLED_OUT, publisherKey, orderKey);
                }
            } else {
                order = this.orderRepository.rejectOrderByProvider(publisherKey, orderKey, rejectReason);

                this.sendOrderStatusByMail(EnumMailType.CONSUMER_PURCHASE_REJECTION, consumerKey, orderKey);

                final EnumNotificationType notificationType = EnumNotificationType.PURCHASE_REJECTED;
                final String               idempotentKey    = orderKey.toString() + "::" + notificationType.toString();
                variables.put("orderKey", orderKey);

                this.sendOrderStatusByNotification(notificationType, consumerKey, variables, idempotentKey);
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

    @Override
    @Transactional
    public ProviderOrderDto uploadContractByProvider(UploadOrderContractCommand command, InputStream input, boolean sendNotification) throws OrderException {
        try {
            Assert.notNull(command, "Expected a non-null command");
            Assert.isTrue(command.getSize() > 0, "Expected file size to be greater than 0");

            // Update order status
            final ProviderOrderDto order = this.orderRepository.uploadContractByProvider(
                command.getProviderKey(), command.getOrderKey(), command.isLastUpdate()
            );

            // Save contract
            this.saveUploadedContract(order.getConsumer().getId(), command, false, input);

            // Send email notification to consumer
            if (sendNotification) {
                final UUID consumerKey = order.getConsumer().getKey();
                this.sendOrderStatusByMail(EnumMailType.CONSUMER_FILLED_OUT_CONTRACT, consumerKey, command.getOrderKey());
            }

            return order;
        } catch (final OrderException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw new OrderException(OrderMessageCode.ERROR, "An unknown error has occurred", ex);
        }
    }

    private void saveUploadedContract(
        Integer userId, UploadOrderContractCommand command, boolean signed, InputStream input
    ) throws AssetRepositoryException, FileSystemException {
        try {
            final Path absolutePath = this.contractFileManager.resolveUploadedContractPath(
                userId, command.getOrderKey(), command.getItemIndex(), signed
            );
            final File localFile    = absolutePath.toFile();

            // Create parent directories
            if (!absolutePath.getParent().toFile().exists()) {
                Files.createDirectories(absolutePath.getParent());
                Files.setPosixFilePermissions(absolutePath.getParent(), DEFAULT_DIRECTORY_PERMISSIONS);
            }

            if (localFile.exists()) {
                FileUtils.deleteQuietly(localFile);
            }

            Files.copy(input, absolutePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public Path resolveOrderContractPath(UUID providerKey, UUID orderKey) {
        final ProviderOrderDto order = this.orderRepository.findObjectByProviderAndKeyAndStatusNotCreated(providerKey, orderKey).orElse(null);
        if (order == null) {
            return null;
        }
        final Integer userId              = order.getConsumer().getId();
        final Path    initialContractPath = this.contractFileManager.resolveUploadedContractPath(userId, orderKey, 1, false);
        final Path    signedContractPath  = this.contractFileManager.resolveUploadedContractPath(userId, orderKey, 1, true);

        if (signedContractPath.toFile().exists()) {
            return signedContractPath;
        }
        if (initialContractPath.toFile().exists()) {
            return initialContractPath;
        }
        return null;
    }

    @Override
    public ConsumerOrderDto acceptContractByConsumer(AcceptOrderContractCommand command) throws OrderException {
        try {
            final ConsumerOrderDto order = this.orderRepository.acceptContractByConsumer(command.getConsumerKey(), command.getOrderKey());
            this.sendOrderStatusByMail(EnumMailType.CONSUMER_SUPPLIER_CONTRACT_SIGNED, command.getConsumerKey(), command.getOrderKey());

            final UUID providerKey = order.getItems().get(0).getProvider().getKey();
            this.sendOrderStatusByMail(EnumMailType.CONSUMER_SUPPLIER_CONTRACT_SIGNED, providerKey, command.getOrderKey());
            return order;
        } catch (final OrderException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw new OrderException(OrderMessageCode.ERROR, "An unknown error has occurred", ex);
        }
    }

    /**
     * Initializes a workflow instance to process the referenced order
     *
     * The operation may fail because of (a) a network error, (b) BPM engine
     * service error or (c) database command error. The operation is retried for
     * at most 3 times, with a maximum latency due to attempt delays of 9
     * seconds.
     */
    @Override
    @Retryable(include = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, maxDelay = 3000))
    public String startOrderWithoutPayInWorkflow(UUID payInKey) {
        final EnumWorkflow workflow = EnumWorkflow.CONSUMER_PURCHASE_ASSET_WITHOUT_PAYIN;

        try {
            final HelpdeskPayInDto payIn = payInRepository.findOneObjectByKey(payInKey).orElse(null);
            Assert.isTrue(payIn.getItems().size() == 1, "Expected a single pay in item");
            Assert.isTrue(payIn.getTotalPrice().compareTo(BigDecimal.ZERO) == 0, "Expected total price to be 0");

            final EnumPaymentItemType type = payIn.getItems().get(0).getType();
            Assert.isTrue(type == EnumPaymentItemType.ORDER, "Expected an order pay in item");

            final HelpdeskOrderPayInItemDto payInItem = (HelpdeskOrderPayInItemDto) payIn.getItems().get(0);
            final HelpdeskOrderDto          order     = payInItem.getOrder();

            if (!StringUtils.isBlank(payIn.getProcessInstance())) {
                // Workflow instance already exists
                return payIn.getProcessInstance();
            }

            ProcessInstanceDto instance = this.bpmEngine.findInstance(payInKey.toString());
            if (instance == null) {
                // Set business key
                final String businessKey= payInKey.toString();

                // Set variables
                final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                    .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), "")
                    .variableAsString("payInKey", payInKey.toString())
                    .variableAsString("orderKey", order.getKey().toString())
                    .variableAsString("orderReferenceNumber", order.getReferenceNumber())
                    .variableAsString("consumerKey", payIn.getConsumerKey().toString())
                    .variableAsString("providerKey", payIn.getProviderKey().get(0).toString())
                    .variableAsString("payInStatus", EnumTransactionStatus.SUCCEEDED.toString())
                    .variableAsString("deliveryMethod", payInItem.getOrder().getDeliveryMethod().toString())
                    .build();

                instance = this.bpmEngine.startProcessDefinitionByKey(workflow, businessKey, variables);
            }

            payInRepository.setPayInWorkflowInstance(payIn.getId(), instance.getDefinitionId(), instance.getId());

            return instance.getId();
        } catch(final Exception ex) {
            logger.error(
                    "Failed to start workflow instance [workflow={}, businessKey={}, ex={}]", workflow, payInKey, ex.getMessage()
            );
        }

        return null;
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
    public String startOrderWithPayInWorkflow(UUID payInKey, String payInId, EnumTransactionStatus payInStatus) {
        final EnumWorkflow workflow = EnumWorkflow.CONSUMER_PURCHASE_ASSET_WITH_PAYIN;

        try {
            final HelpdeskPayInDto payIn = payInRepository.findOneObjectByKey(payInKey).orElse(null);
            Assert.isTrue(payIn.getItems().size() == 1, "Expected a single pay in item");
            final EnumPaymentItemType type = payIn.getItems().get(0).getType();
            Assert.isTrue(type == EnumPaymentItemType.ORDER, "Expected an order pay in item");
            final HelpdeskOrderPayInItemDto payInItem = (HelpdeskOrderPayInItemDto) payIn.getItems().get(0);
            final HelpdeskOrderDto          order     = payInItem.getOrder();

            if (!StringUtils.isBlank(payIn.getProcessInstance())) {
                // Workflow instance already exists
                return payIn.getProcessInstance();
            }

            ProcessInstanceDto instance = this.bpmEngine.findInstance(payInKey.toString());
            if (instance == null) {
                // Set business key
                final String businessKey= payInKey.toString();

                // Set variables
                final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                    .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), "")
                    .variableAsString("orderKey", order.getKey().toString())
                    .variableAsString("orderReferenceNumber", order.getReferenceNumber())
                    .variableAsString("consumerKey", payIn.getConsumerKey().toString())
                    .variableAsString("providerKey", payIn.getProviderKey().get(0).toString())
                    .variableAsString("payInKey", payInKey.toString())
                    .variableAsString("payInId", payInId)
                    .variableAsString("payInStatus", payInStatus.toString())
                    .variableAsString("deliveryMethod", payInItem.getOrder().getDeliveryMethod().toString())
                    .build();

                instance = this.bpmEngine.startProcessDefinitionByKey(workflow, businessKey, variables);
            }

            payInRepository.setPayInWorkflowInstance(payIn.getId(), instance.getDefinitionId(), instance.getId());

            return instance.getId();
        } catch(final Exception ex) {
            logger.error(
                String.format("Failed to start workflow instance [workflow=%s, businessKey=%s]", workflow, payInKey), ex
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
                    EnumWorkflow.CONSUMER_PURCHASE_ASSET_WITH_PAYIN, payInKey, MESSAGE_UPDATE_PAYIN_STATUS, fex.getMessage()
                );
                throw fex;
            }
        } catch(final Exception ex) {
            logger.error(
                "Failed to send message [workflow={}, businessKey={}, message={}, ex={}]",
                EnumWorkflow.CONSUMER_PURCHASE_ASSET_WITH_PAYIN, payInKey, MESSAGE_UPDATE_PAYIN_STATUS, ex.getMessage()
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
    public ConsumerOrderDto receiveOrderByConsumer(OrderDeliveryCommand command) throws OrderException {
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
    public void registerConsumerAssets(UUID payInKey) throws Exception {
        final PayInEntity payIn = payInRepository.findOneEntityByKey(payInKey).orElse(null);

        // Update account profile
        for (final PayInItemEntity item : payIn.getItems()) {
            switch (item.getType()) {
                case ORDER :
                    final PayInOrderItemEntity orderItem = (PayInOrderItemEntity) item;

                    // Register asset to the platform account
                    this.registerOrderItem(payIn, (PayInOrderItemEntity) item);

                    // Optional register asset to an external data provider
                    this.dataProviderManager.registerAsset(payInKey);

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
        final CatalogueItemDetailsDto asset     = this.catalogueService.findOne(null, orderItem.getAssetId(), null, false);

        if (!asset.getType().isRegisteredOnPurchase()) {
            // No registration required
            return;
        }

        // Update asset statistics
        this.statisticsService.increaseSales(orderItem.getAssetId());

        switch (asset.getType().getOrderItemType()) {
            case ASSET :
                // For bundles, register every asset
                if(asset.getType() == EnumAssetType.BUNDLE) {
                    asset.getResources().stream()
                        .map(r -> {
                            final var result = (r instanceof final BundleAssetResourceDto assetResource)
                                    ? this.catalogueService.findOne(null, assetResource.getId(), null, false)
                                    : null;
                            return result;
                        })
                        .forEach(a -> this.registerAsset(payIn, payInItem, a));
                } else {
                    this.registerAsset(payIn, payInItem, asset);
                }
                break;

            case SUBSCRIPTION :
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
        final ZonedDateTime            purchaseOn   = payIn.getExecutedOn();

        // Check if the order item is already registered
        AccountAssetEntity ownedAsset = accountAssetRepository.findAllByUserKeyAndAssetId(userKey, asset.getId()).stream()
            .filter(a -> !a.getOrder().getId().equals(order.getId()))
            .findFirst().orElse(null);

        if (ownedAsset == null) {
            // Register asset to consumer's account
            ownedAsset = new AccountAssetEntity();


            ownedAsset.setAddedOn(purchaseOn);
            ownedAsset.setAsset(asset.getId());
            ownedAsset.setConsumer(payIn.getConsumer());
            ownedAsset.setOrder(order);
            ownedAsset.setProvider(orderItem.getProvider());
            ownedAsset.setPurchasedOn(purchaseOn);
            ownedAsset.setSource(EnumAssetSource.PURCHASE);
            ownedAsset.setUpdateEligibility(purchaseOn);
            ownedAsset.setUpdateInterval(0);
        }
        // Set or update years of free updates
        if (pricingModel.getModel().getType() == EnumPricingModel.FIXED) {
            final FixedPricingModelCommandDto fixedPricingModel = (FixedPricingModelCommandDto) pricingModel.getModel();
            final Integer                     yearsOfUpdates    = fixedPricingModel.getYearsOfUpdates();

            if (fixedPricingModel.getYearsOfUpdates() > 0) {
                ownedAsset.setUpdateEligibility(purchaseOn.plusYears(yearsOfUpdates));
                ownedAsset.setUpdateInterval(yearsOfUpdates);
            }
        }

        this.accountAssetRepository.save(ownedAsset);
    }

    private void registerSubscription(PayInEntity payIn, PayInOrderItemEntity payInItem, CatalogueItemDetailsDto asset) {
        // An order contains only a single item
        final UUID                     userKey      = payIn.getConsumer().getKey();
        final OrderEntity              order        = payInItem.getOrder();
        final OrderItemEntity          orderItem    = order.getItems().get(0);
        final EffectivePricingModelDto pricingModel = orderItem.getPricingModel();
        final ZonedDateTime            now          = ZonedDateTime.now();

        // Check if a subscription is already active
        final AccountSubscriptionEntity activeSubscription = accountSubscriptionRepository.findAllByConsumerAndAssetId(userKey, asset.getId()).stream()
            .filter(s -> !s.getOrder().getId().equals(order.getId()))
            .findFirst().orElse(null);
        final boolean renewal = activeSubscription != null;

        // Create/Update subscription for consumer account
        AccountSubscriptionEntity sub;
        if (renewal) {
            sub = activeSubscription;
            sub.setUpdatedOn(now);
        } else {
            sub = new AccountSubscriptionEntity();

            sub.setAddedOn(now);
            sub.setAssetId(asset.getId());
            sub.setAssetTitle(orderItem.getAssetTitle());
            sub.setAssetVersion(orderItem.getAssetVersion());
            sub.setCancelledOn(null);
            sub.setConsumer(payIn.getConsumer());
            sub.setExpiresOn(null);
            sub.setLastPayinDate(order.getPayin().getExecutedOn());
            sub.setNextPayinDate(null);
            sub.setOrder(order);
            sub.setProvider(orderItem.getProvider());
            sub.setSegment(asset.getTopicCategory().stream().findFirst().orElse(null));
            sub.setSource(EnumAssetSource.PURCHASE);
            sub.setStatus(EnumSubscriptionStatus.ACTIVE);
            sub.setUpdatedOn(now);

            if (payIn.getPaymentMethod() == EnumPaymentMethod.CARD_DIRECT) {
                final CardDirectPayInEntity payInCardDirect = (CardDirectPayInEntity) payIn;
                sub.setRecurringPayIn(payInCardDirect.getRecurringPayment());
            }
        }

        // Register call/rows SKUs
        final QuotationParametersDto params = pricingModel.getUserParameters();
        AccountSubscriptionSkuEntity sku    = null;

        switch (pricingModel.getModel().getType()) {
            case PER_CALL : {
                final PerCallPricingModelCommandDto prePaidCallModel = (PerCallPricingModelCommandDto) pricingModel.getModel();
                final PerCallQuotationParametersDto typedParams1     = (PerCallQuotationParametersDto) params;
                final Integer                       callTierIndex    = typedParams1.getPrePaidTier();
                if (callTierIndex != null) {
                    final PrePaidTierDto callTier = prePaidCallModel.getPrePaidTiers().get(callTierIndex);
                    sku = new AccountSubscriptionSkuEntity();
                    sku.setPurchasedCalls(callTier.getCount());
                }
                break;
            }

            case PER_ROW : {
                final PerRowPricingModelCommandDto prePaidRowModel = (PerRowPricingModelCommandDto) pricingModel.getModel();
                final PerRowQuotationParametersDto typedParams2    = (PerRowQuotationParametersDto) params;
                final Integer                      rowTierIndex    = typedParams2.getPrePaidTier();
                if (rowTierIndex != null) {
                    final PrePaidTierDto rowTier = prePaidRowModel.getPrePaidTiers().get(rowTierIndex);
                    sku = new AccountSubscriptionSkuEntity();
                    sku.setPurchasedRows(rowTier.getCount());
                }
                break;
            }

            default :
                // No action is required
                break;
        }
        if (sku != null) {
            sku.setOrder(order);
            sku.setSubscription(sub);

            sub.getSkus().add(sku);
        }

        this.accountSubscriptionRepository.save(sub);

        // Link subscription to recurring payment if one exists
        if (payIn instanceof CardDirectPayInEntity) {
            final CardDirectPayInEntity cardPayIn = (CardDirectPayInEntity) payIn;

            // Optionally update subscription for recurring payment
            // registration, if one exists
            if (cardPayIn.getRecurringPayment() != null) {
                cardPayIn.getRecurringPayment().setSubscription(sub);
            }

            this.payInRepository.saveAndFlush(cardPayIn);
        }
    }

    @Override
    @Transactional
    public void sendOrderStatusByMail(EnumMailType mailType, UUID recipientKey, UUID orderKey) {
        // Resolve recipient
        final AccountEntity account = accountRepository.findOneByKey(recipientKey).orElse(null);
        if (account == null) {
            throw new ServiceException(
                MailMessageCode.RECIPIENT_NOT_FOUND,
                String.format("Recipient was not found [userKey=%s]", recipientKey)
            );
        }
        // Compose message
        final MailModelBuilder builder = MailModelBuilder.builder()
            .add("userKey", recipientKey.toString())
            .add("orderKey", orderKey.toString());

        final Map<String, Object>             model    = this.messageHelper.createModel(mailType, builder);
        final EmailAddressDto                 sender   = this.messageHelper.getSender(mailType, model);
        final String                          subject  = this.messageHelper.composeSubject(mailType, model);
        final String                          template = this.messageHelper.resolveTemplate(mailType, model);
        final MessageDto<Map<String, Object>> message  = new MessageDto<>();

        message.setSender(sender);
        message.setSubject(subject);
        message.setTemplate(template);
        message.setModel(model);

        message.setRecipients(builder.getAddress());

        try {
            final ResponseEntity<BaseResponse> response = this.mailClient.getObject().sendMail(message);

            if (!response.getBody().getSuccess()) {
                throw new ServiceException(
                    MailMessageCode.SEND_MAIL_FAILED,
                    String.format("Failed to send mail [userKey=%s]", recipientKey)
                );
            }
        } catch (final FeignException fex) {
            throw new ServiceException(
                MailMessageCode.SEND_MAIL_FAILED,
                String.format("Failed to send mail [userKey=%s]", recipientKey),
                fex
            );
        }
    }

    @Override
    @Transactional
    public void sendOrderStatusByNotification(
        EnumNotificationType type, UUID recipientKey, Map<String, Object>  variables, String idempotentKey
    ) {
        try {
            // Build notification message
            final JsonNode data = this.notificationMessageBuilder.collectNotificationData(type, variables);

            final ServerNotificationCommandDto notification = ServerNotificationCommandDto.builder()
                .data(data)
                .eventType(type.toString())
                .idempotentKey(idempotentKey)
                .recipient(recipientKey)
                .text(this.notificationMessageBuilder.composeNotificationText(type, data))
                .build();

            messageClient.getObject().sendNotification(notification);
        } catch (final FeignException fex) {
            throw new ServiceException(
                MailMessageCode.SEND_MAIL_FAILED,
                String.format("Failed to send mail [userKey=%s]", recipientKey),
                fex
            );
        }
    }
}
