package eu.opertusmundi.common.service.mangopay;

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

import eu.opertusmundi.common.domain.PayOutEntity;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.model.workflow.EnumWorkflow;
import eu.opertusmundi.common.repository.PayOutRepository;
import eu.opertusmundi.common.util.BpmEngineUtils;
import eu.opertusmundi.common.util.BpmInstanceVariablesBuilder;
import feign.FeignException;

/**
 * Utility service that implements retryable operations for managing MANGOPAY
 * PayOut workflow instances
 */
@Service
@Transactional
public class DefaultPayOutWorkflowService implements PayOutWorkflowService {

    private static final String MESSAGE_UPDATE_PAYOUT_STATUS = "payout-updated-message";

    private static final Logger logger = LoggerFactory.getLogger(DefaultPayOutWorkflowService.class);

    private final PayOutRepository payOutRepository;
    private final BpmEngineUtils   bpmEngine;

    @Autowired
    public DefaultPayOutWorkflowService(
        BpmEngineUtils   bpmEngine,
        PayOutRepository payOutRepository
    ) {
        this.bpmEngine        = bpmEngine;
        this.payOutRepository = payOutRepository;
    }

    @Override
    @Retryable(include = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, maxDelay = 3000))
    public String start(UUID userKey, UUID payOutKey) {
        final EnumWorkflow workflow = EnumWorkflow.PROVIDER_PAYOUT;

        try {
            final PayOutEntity payOut = payOutRepository.findOneEntityByKey(payOutKey).orElse(null);

            if (!StringUtils.isBlank(payOut.getProcessInstance())) {
                // Workflow instance already exists
                return payOut.getProcessInstance();
            }

            ProcessInstanceDto instance = this.bpmEngine.findInstance(payOutKey.toString());

            if (instance == null) {
                final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                    .variableAsUuid(EnumProcessInstanceVariable.START_USER_KEY.getValue(), userKey)
                    .variableAsUuid("providerKey", payOut.getProvider().getKey())
                    .variableAsString("providerName", payOut.getProvider().getEmail())
                    .variableAsUuid("payOutKey", payOutKey)
                    .variableAsString("payOutBankwireRef", payOut.getBankwireRef())
                    .variableAsString("payOutId", null)
                    .variableAsString("payOutStatus", payOut.getStatus().toString())
                    .build();


                instance = this.bpmEngine.startProcessDefinitionByKey(workflow, payOutKey.toString(), variables, true);
            }

            payOutRepository.setPayOutWorkflowInstance(payOut.getId(), instance.getDefinitionId(), instance.getId());

            return instance.getId();
        } catch(final Exception ex) {
            logger.error(
                "Failed to start workflow instance [workflow={}, businessKey={}, ex={}]",
                workflow, payOutKey, ex.getMessage()
            );
        }

        return null;
    }

    @Override
    @Retryable(include = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, maxDelay = 3000))
    public void sendStatusUpdateMessage(UUID payOutKey, EnumTransactionStatus status) {
        final String messageName = MESSAGE_UPDATE_PAYOUT_STATUS;

        try {
            final String businessKey = payOutKey.toString();

            final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                .variableAsString("payOutStatus", status.toString())
                .build();

            this.bpmEngine.correlateMessage(businessKey, messageName, variables);

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
                    EnumWorkflow.PROVIDER_PAYOUT, payOutKey, messageName, fex.getMessage()
                );
                throw fex;
            }
        } catch(final Exception ex) {
            logger.error(
                "Failed to send message [workflow={}, businessKey={}, message={}, ex={}]",
                EnumWorkflow.PROVIDER_PAYOUT, payOutKey, messageName, ex.getMessage()
            );
            throw ex;
        }
    }

}
