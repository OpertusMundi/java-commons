package eu.opertusmundi.common.service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountAssetEntity;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInItemEntity;
import eu.opertusmundi.common.domain.PayInOrderItemEntity;
import eu.opertusmundi.common.feign.client.BpmServerFeignClient;
import eu.opertusmundi.common.model.account.EnumAssetSource;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import eu.opertusmundi.common.model.pricing.FixedPricingModelCommandDto;
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.repository.AccountAssetRepository;
import eu.opertusmundi.common.repository.PayInRepository;
import feign.FeignException;

@Service
@Transactional
public class DefaultOrderFulfillmentService implements OrderFulfillmentService {

    private static final String WORKFLOW_PROCESS_PAYIN = "workflow-process-payin";

    private static final String MESSAGE_UPDATE_PAYIN_STATUS = "payin-updated-message";

    private static final Logger logger = LoggerFactory.getLogger(DefaultOrderFulfillmentService.class);

    @Autowired
    private PayInRepository payInRepository;

    @Autowired
    private AccountAssetRepository accountAssetRepository;

    @Autowired
    private ObjectProvider<BpmServerFeignClient> bpmClient;

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
    public String start(UUID payInKey) {
        try {
            final PayInEntity payIn = payInRepository.findOneEntityByKey(payInKey).orElse(null);

            if (!StringUtils.isBlank(payIn.getProcessInstance())) {
                // Workflow instance already exists
                return payIn.getProcessInstance();
            }

            ProcessInstanceDto instance = this.findRunningInstance(payInKey.toString());
            if (instance == null) {
                // Start new instance
                final StartProcessInstanceDto options = new StartProcessInstanceDto();

                final Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();

                // Set variables
                this.setStringVariable(variables, EnumProcessInstanceVariable.START_USER_KEY.getValue(), "");
                this.setStringVariable(variables, "payInKey", payInKey);

                options.setBusinessKey(payInKey.toString());
                options.setVariables(variables);
                options.setWithVariablesInReturn(true);

                instance = this.bpmClient.getObject().startProcessDefinitionByKey(WORKFLOW_PROCESS_PAYIN, options);
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
            final PayInEntity payIn       = payInRepository.findOneEntityByKey(payInKey).orElse(null);
            final String      businessKey = payIn.getKey().toString();

            final CorrelationMessageDto         message   = new CorrelationMessageDto();
            final Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();

            this.setStringVariable(variables, "payInStatus", status.toString());

            message.setMessageName(MESSAGE_UPDATE_PAYIN_STATUS);
            message.setBusinessKey(businessKey);
            message.setProcessVariables(variables);
            message.setVariablesInResultEnabled(true);
            message.setResultEnabled(true);

            this.bpmClient.getObject().correlateMessage(message);
        } catch (final FeignException fex) {
            if (fex.status() != HttpStatus.BAD_REQUEST.value()) {
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
            }
        } catch(final Exception ex) {
            logger.error(
                "Failed to send message [workflow={}, businessKey={}, message={}, ex={}]",
                WORKFLOW_PROCESS_PAYIN, payInKey, MESSAGE_UPDATE_PAYIN_STATUS, ex.getMessage()
            );
        }

    }

    /**
     * Update user profile with purchased assets and subscriptions
     *
     * This operation is invoked by workflow instances and will be retried by
     * the BPM engine
     */
    @Override
    public void updateConsumer(UUID payInKey) {
        final PayInEntity payIn = payInRepository.findOneEntityByKey(payInKey).orElse(null);

        for (final PayInItemEntity item : payIn.getItems()) {
            switch (item.getType()) {
                case ORDER :
                    this.registerAsset(payIn, (PayInOrderItemEntity) item);
                    break;
                default :
                    throw new PaymentException(PaymentMessageCode.PAYIN_ITEM_TYPE_NOT_SUPPORTED, String.format(
                        "PayIn item type not supported [payIn=%s, index=%d, type=%s]",
                        payInKey, item.getIndex(), item.getType(
                    )));
            }
        }
    }

    private void registerAsset(PayInEntity payIn, PayInOrderItemEntity payInItem) {
        // An order contains only a single item
        final UUID                     userKey      = payIn.getConsumer().getKey();
        final OrderEntity              order        = payInItem.getOrder();
        final OrderItemEntity          orderItem    = order.getItems().get(0);
        final EffectivePricingModelDto pricingModel = orderItem.getPricingModel();

        // Check if the order item is already registered
        final AccountAssetEntity ownedAsset = accountAssetRepository.findAllByUserKeyAndAssetId(userKey, orderItem.getItem()).stream()
            .filter(a -> a.getOrder().getId() == order.getId())
            .findFirst().orElse(null);
        if (ownedAsset != null) {
            return;
        }

        // Register asset to consumer's account
        final AccountAssetEntity reg = new AccountAssetEntity();

        reg.setAddedOn(ZonedDateTime.now());
        reg.setAsset(orderItem.getItem());
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

    /**
     * Find running workflow instance by business key
     *
     * @param businessKey
     * @return
     */
    private ProcessInstanceDto findRunningInstance(String businessKey) {
        final List<ProcessInstanceDto> instances = this.bpmClient.getObject().getProcessInstances(businessKey);

        return instances.stream()
            .filter(i -> !i.isEnded())
            .findFirst()
            .orElse(null);
    }

    private void setStringVariable(Map<String, VariableValueDto> variables, String name, Object value) {
        this.setVariable(variables, "String", name, value);
    }

    private void setVariable(Map<String, VariableValueDto> variables, String type, String name, Object value) {
        final VariableValueDto v = new VariableValueDto();

        v.setValue(value);
        v.setType(type);

        variables.put(name, v);
    }

}
