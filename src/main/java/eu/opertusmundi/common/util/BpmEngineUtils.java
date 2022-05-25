package eu.opertusmundi.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.externaltask.SetRetriesForExternalTasksDto;
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.feign.client.BpmServerFeignClient;
import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.workflow.EnumWorkflow;
import feign.FeignException;

@Service
public class BpmEngineUtils {

    private static final Logger logger = LoggerFactory.getLogger(BpmEngineUtils.class);

    @Autowired
    private ObjectProvider<BpmServerFeignClient> bpmClient;

    public ProcessInstanceDto findInstance(UUID businessKey) throws ApplicationException {
        return this.findInstance(businessKey.toString());
    }

    public ProcessInstanceDto findInstance(String businessKey) throws ApplicationException {
        try {
            final List<ProcessInstanceDto> instances = this.bpmClient.getObject().getProcessInstances(null, businessKey);

            return instances.stream()
                .filter(i -> !i.isEnded())
                .findFirst()
                .orElse(null);
        } catch (final FeignException fex) {
            logger.error("[Feign Client] Operation has failed", fex);

            // Handle 404 errors as valid responses
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            throw ApplicationException.fromMessage(fex, BasicMessageCode.BpmServiceError, "Operation on BPM server failed");
        }
    }

    public ProcessInstanceDto startProcessDefinitionByKey(
        EnumWorkflow workflow, String businessKey, Map<String, VariableValueDto> variables
    ) {
        return this.startProcessDefinitionByKey(workflow, businessKey, variables, true);
    }

    public ProcessInstanceDto startProcessDefinitionByKey(
        EnumWorkflow workflow, String businessKey, Map<String, VariableValueDto> variables, boolean withVariablesInReturn
    ) {
        final StartProcessInstanceDto options = new StartProcessInstanceDto();

        options.setBusinessKey(businessKey);
        options.setVariables(variables);
        options.setWithVariablesInReturn(withVariablesInReturn);

        return this.bpmClient.getObject().startProcessDefinitionByKey(workflow.getKey(), options);
    }

    public Optional<TaskDto> findTaskById(String businessKey, String taskId) {
        final List<TaskDto> tasks = this.bpmClient.getObject().findTaskById(businessKey, taskId);

        if (tasks == null || tasks.isEmpty() || tasks.size() != 1) {
            return Optional.ofNullable(null);
        }
        return Optional.of(tasks.get(0));
    }

    public void completeTask(String taskId, Map<String, VariableValueDto> variables) {
        final CompleteTaskDto options = new CompleteTaskDto();

        options.setVariables(variables);

        this.bpmClient.getObject().completeTask(taskId, options);
    }

    public void correlateMessage(String businessKey, String messageName, Map<String, VariableValueDto> variables) {
        final CorrelationMessageDto message = new CorrelationMessageDto();

        message.setBusinessKey(businessKey);
        message.setMessageName(messageName);
        message.setProcessVariables(variables);
        message.setVariablesInResultEnabled(true);
        message.setResultEnabled(true);

        this.bpmClient.getObject().correlateMessage(message);
    }

    public void retryExternalTask(String processInstanceId, String externalTaskId) {
        final SetRetriesForExternalTasksDto request            = new SetRetriesForExternalTasksDto();
        final List<String>                  processInstanceIds = Arrays.asList(processInstanceId);
        final List<String>                  externalTaskIds    = Arrays.asList(externalTaskId);

        request.setProcessInstanceIds(processInstanceIds);
        request.setExternalTaskIds(externalTaskIds);
        request.setRetries(1);

        this.bpmClient.getObject().setExternalTaskRetries(request);
    }

    public void deleteProcessInstance(String processInstanceId) {
        this.bpmClient.getObject().deleteProcessInstance(processInstanceId);
    }

    public void deleteHistoryProcessInstance(String processInstanceId) {
        this.bpmClient.getObject().deleteHistoryProcessInstance(processInstanceId);
    }

}
