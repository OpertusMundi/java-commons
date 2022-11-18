package eu.opertusmundi.common.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.ModificationDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.externaltask.SetRetriesForExternalTasksDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricActivityInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricIncidentDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceDto;
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDiagramDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.IncidentDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceWithVariablesDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import eu.opertusmundi.common.feign.client.BpmServerFeignClient;
import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.workflow.EnumWorkflow;
import feign.FeignException;

@Service
public final class BpmEngineUtils {

    private static final Logger logger = LoggerFactory.getLogger(BpmEngineUtils.class);

    @Autowired
    private ObjectProvider<BpmServerFeignClient> bpmClient;

    public List<org.camunda.bpm.engine.rest.dto.repository.DeploymentDto> getDeployments(String sortOrder, String sortBy) {
        var result = this.bpmClient.getObject().getDeployments(sortOrder, sortBy);

        return result;
    }

    public void deleteDeployment(String id, boolean cascade) {
        this.bpmClient.getObject().deleteDeployment(id, cascade);
    }

    public List<ProcessDefinitionDto> getProcessDefinitions(
        ProcessDefinitionQueryDto query,
        int firstResult,
        int maxResults
    ) {
        return this.bpmClient.getObject().getProcessDefinitions(query, firstResult, maxResults);
    }

    public String getBpmnXml(@PathVariable("id") String processDefinitionId) {
        final ProcessDefinitionDiagramDto result = this.bpmClient.getObject().getBpmnXml(processDefinitionId).getBody();

        return result == null ? null : result.getBpmn20Xml();
    }

    public long countProcessInstances(String deploymentId) {
        return this.bpmClient.getObject().countProcessInstances(deploymentId).getCount();
    }

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

    public List<HistoricProcessInstanceDto> findHistoryInstances(
        String processDefinitionKey, String businessKey, String processInstanceId
    ) {
        return this.bpmClient.getObject().getHistoryProcessInstances(processDefinitionKey, businessKey, processInstanceId);
    }

    public List<HistoricProcessInstanceDto> findHistoryInstances(UUID businessKey) throws ApplicationException {
        return this.findHistoryInstances(businessKey.toString());
    }

    public List<HistoricProcessInstanceDto> findHistoryInstances(String businessKey) throws ApplicationException {
        try {
            final List<HistoricProcessInstanceDto> instances = this.bpmClient.getObject().getHistoryProcessInstances(null, businessKey, null);

            return instances.stream()
                .filter(i -> i.getEndTime() != null)
                .collect(Collectors.toList());
        } catch (final FeignException fex) {
            logger.error("[Feign Client] Operation has failed", fex);

            // Handle 404 errors as valid responses
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            throw ApplicationException.fromMessage(fex, BasicMessageCode.BpmServiceError, "Operation on BPM server failed");
        }
    }

    public List<ProcessInstanceDto> findInstancesByProcessDefinitionKey(String processDefinitionKey) throws ApplicationException {
        try {
            final List<ProcessInstanceDto> instances = this.bpmClient.getObject().getProcessInstances(processDefinitionKey, null);

            return instances.stream()
                .filter(i -> !i.isEnded())
                .collect(Collectors.toList());
        } catch (final FeignException fex) {
            logger.error("[Feign Client] Operation has failed", fex);

            // Handle 404 errors as valid responses
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return Collections.emptyList();
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

    public ProcessInstanceWithVariablesDto modifyProcessInstance(String processInstanceId, ModificationDto modification) {
        return this.bpmClient.getObject().modifyProcessInstance(processInstanceId, modification);
    }

    public long countProcessInstanceTasks() {
        final CountResultDto result = this.bpmClient.getObject().countProcessInstanceTasks();

        return result.getCount();
    }

    public Optional<TaskDto> findTask(String businessKey, String taskDefinitionKey) {
        final List<TaskDto> tasks = this.bpmClient.getObject().getTasks(businessKey, taskDefinitionKey);

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

    public String getExternalTaskErrorDetails(String activityId) {
        return this.bpmClient.getObject().getExternalTaskErrorDetails(activityId);
    }

    public String getHistoryExternalTaskLogErrorDetails(String logId) {
        return this.bpmClient.getObject().getHistoryExternalTaskLogErrorDetails(logId);
    }

    public void retryExternalTask(String processInstanceId, String externalTaskId, int retryCount) {
        final SetRetriesForExternalTasksDto request            = new SetRetriesForExternalTasksDto();
        final List<String>                  processInstanceIds = Arrays.asList(processInstanceId);
        final List<String>                  externalTaskIds    = Arrays.asList(externalTaskId);

        request.setProcessInstanceIds(processInstanceIds);
        request.setExternalTaskIds(externalTaskIds);
        request.setRetries(retryCount);

        this.bpmClient.getObject().setExternalTaskRetries(request);
    }

    public long countIncidents() {
        final CountResultDto result = this.bpmClient.getObject().countIncidents();

        return result.getCount();
    }

    public List<IncidentDto> getIncidents(
        String incidentId, String processDefinitionId, String processInstanceId, String executionId,
        String sortBy, String sortOrder
    ) {
        return this.bpmClient.getObject().getIncidents(
            incidentId, processDefinitionId, processInstanceId, executionId, sortBy, sortOrder
        );
    }

    public List<HistoricIncidentDto> getHistoryIncidents(String processInstanceId) {
        return this.bpmClient.getObject().getHistoryIncidents(processInstanceId);
    }

    public List<HistoricActivityInstanceDto> getHistoryProcessInstanceActivityInstances(String processInstanceId) {
        return this.bpmClient.getObject().getHistoryProcessInstanceActivityInstances(processInstanceId);
    }

    public Map<String, VariableValueDto> getProcessInstanceVariables(String processInstanceId) {
        return this.bpmClient.getObject().getProcessInstanceVariables(processInstanceId);
    }

    public List<HistoricVariableInstanceDto> getHistoryProcessInstanceVariables(String processInstanceId) {
        return this.bpmClient.getObject().getHistoryProcessInstanceVariables(processInstanceId);
    }

    public List<VariableInstanceDto> getHistoryVariables(String variableName, String variableValue) {
        return this.bpmClient.getObject().getHistoryVariables(variableName, variableValue);
    }

    public List<VariableInstanceDto> getVariables(String variableName, String variableValue) {
        final String variableValues = variableName + "_eq_" + variableValue;
        return this.bpmClient.getObject().getVariables(variableValues);
    }

    public void deleteProcessInstance(String processInstanceId) {
        this.bpmClient.getObject().deleteProcessInstance(processInstanceId);
    }

    public void deleteHistoryProcessInstance(String processInstanceId) {
        this.bpmClient.getObject().deleteHistoryProcessInstance(processInstanceId);
    }

}
