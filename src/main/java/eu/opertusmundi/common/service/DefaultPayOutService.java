package eu.opertusmundi.common.service;

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

import eu.opertusmundi.common.domain.PayOutEntity;
import eu.opertusmundi.common.feign.client.BpmServerFeignClient;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.repository.PayOutRepository;
import feign.FeignException;

@Service
@Transactional
public class DefaultPayOutService implements PayOutService {

    private static final String WORKFLOW_PROCESS_PAYOUT = "workflow-process-payout";

    private static final String MESSAGE_UPDATE_PAYOUT_STATUS = "payout-updated-message";

    private static final Logger logger = LoggerFactory.getLogger(DefaultPayOutService.class);

    @Autowired
    private PayOutRepository payOutRepository;

    @Autowired
    private ObjectProvider<BpmServerFeignClient> bpmClient;

    /**
     * Initializes a workflow instance to process the referenced PayOut
     *
     * The operation may fail because of (a) a network error, (b) BPM engine
     * service error or (c) database command error. The operation is retried for
     * at most 3 times, with a maximum latency due to attempt delays of 9
     * seconds.
     */
    @Override
    @Retryable(include = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, maxDelay = 3000))
    public String start(UUID userKey, UUID payOutKey) {
        try {
            final PayOutEntity payOut = payOutRepository.findOneEntityByKey(payOutKey).orElse(null);

            if (!StringUtils.isBlank(payOut.getProcessInstance())) {
                // Workflow instance already exists
                return payOut.getProcessInstance();
            }

            ProcessInstanceDto instance = this.findRunningInstance(payOut.toString());
            if (instance == null) {
                // Start new instance
                final StartProcessInstanceDto options = new StartProcessInstanceDto();

                final Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();

                // Set variables
                this.setStringVariable(variables, EnumProcessInstanceVariable.START_USER_KEY.getValue(), userKey.toString());
                this.setStringVariable(variables, "providerKey", payOut.getProvider().getKey());
                this.setStringVariable(variables, "payOutKey", payOutKey);
                this.setStringVariable(variables, "payOutId", null);
                this.setStringVariable(variables, "payOutStatus", payOut.getStatus().toString());

                options.setBusinessKey(payOutKey.toString());
                options.setVariables(variables);
                options.setWithVariablesInReturn(true);

                instance = this.bpmClient.getObject().startProcessDefinitionByKey(WORKFLOW_PROCESS_PAYOUT, options);
            }

            payOutRepository.setPayOutWorkflowInstance(payOut.getId(), instance.getDefinitionId(), instance.getId());

            return instance.getId();
        } catch(final Exception ex) {
            logger.error(
                "Failed to start workflow instance [workflow={}, businessKey={}, ex={}]",
                WORKFLOW_PROCESS_PAYOUT, payOutKey, ex.getMessage()
            );
        }

        return null;
    }

    @Override
    @Retryable(include = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, maxDelay = 3000))
    public void sendPayOutStatusUpdateMessage(UUID payOutKey, EnumTransactionStatus status) {
        try {
            final String       businessKey = payOutKey.toString();

            final CorrelationMessageDto         message   = new CorrelationMessageDto();
            final Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();

            this.setStringVariable(variables, "payOutStatus", status.toString());

            message.setMessageName(MESSAGE_UPDATE_PAYOUT_STATUS);
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
                    WORKFLOW_PROCESS_PAYOUT, payOutKey, MESSAGE_UPDATE_PAYOUT_STATUS, fex.getMessage()
                );
                throw fex;
            }
        } catch(final Exception ex) {
            logger.error(
                "Failed to send message [workflow={}, businessKey={}, message={}, ex={}]",
                WORKFLOW_PROCESS_PAYOUT, payOutKey, MESSAGE_UPDATE_PAYOUT_STATUS, ex.getMessage()
            );
            throw ex;
        }
    }

    /**
     * Find running workflow instance by business key
     *
     * @param businessKey
     * @return
     */
    private ProcessInstanceDto findRunningInstance(String businessKey) {
        final List<ProcessInstanceDto> instances = this.bpmClient.getObject().getProcessInstances(null, businessKey);

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
