package eu.opertusmundi.common.feign.client;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.externaltask.SetRetriesForExternalTasksDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricActivityInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricExternalTaskLogDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricIncidentDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceDto;
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto;
import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultWithVariableDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.ActivityInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.IncidentDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceWithVariablesDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.feign.client.config.BpmServerFeignClientConfiguration;

/**
 * Feign client for Camunda BPM server.
 *
 * @see: https://github.com/camunda/camunda-rest-client-spring-boot
 *
 */
@FeignClient(
    name = "${opertusmundi.feign.bpm-server.name}",
    url = "${opertusmundi.feign.bpm-server.url}",
    configuration = BpmServerFeignClientConfiguration.class
)
public interface BpmServerFeignClient {

    /**
     * Queries for process definitions that fulfill given parameters. Parameters
     * may be the properties of process definitions, such as the name, key or
     * version.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/process-definition/get-query/
     */
    @GetMapping(value = "/process-definition", consumes = "application/json")
    List<ProcessDefinitionDto> getProcessDefinitions(
        @SpringQueryMap ProcessDefinitionQueryDto query,
        @RequestParam("firstResult") int firstResult,
        @RequestParam("maxResults") int maxResults
    );

    /**
     * Instantiates a given process definition. Process variables and business
     * key may be supplied in the request body.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/process-definition/post-start-process-instance/
     */
    @PostMapping(value = "/process-definition/key/{key}/start", consumes = "application/json")
    ProcessInstanceWithVariablesDto startProcessDefinitionByKey(
        @PathVariable("key") String processDefinitionKey,
        StartProcessInstanceDto startProcessInstance
    );

    /**
     * Queries for the number of process instances that fulfill given parameters.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/get-query-count/
     */
    @PostMapping(value = "/process-instance/count", consumes = "application/json")
    CountResultDto countProcessInstances();

    /**
     * Queries for process instances that fulfill given parameters. Parameters
     * may be static as well as dynamic runtime properties of process instances.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/get/
     */
    @GetMapping(value = "/process-instance", consumes = "application/json")
    List<ProcessInstanceDto> getProcessInstances(
        @RequestParam("processDefinitionKey") String processDefinitionKey,
        @RequestParam("businessKey") String businessKey
    );

    /**
     * Queries for process instances that fulfill given parameters. Parameters
     * may be static as well as dynamic runtime properties of process instances.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/history/process-instance/get-process-instance-query/
     */
    @GetMapping(value = "/history/process-instance", consumes = "application/json")
    List<HistoricProcessInstanceDto> getHistoryProcessInstances(
        @RequestParam(name = "processDefinitionKey", required = false) String processDefinitionKey,
        @RequestParam(name = "processInstanceBusinessKey", required = false) String businessKey,
        @RequestParam(name = "processInstanceId", required = false) String processInstanceId
    );

    /**
     * Retrieves all variables of a given process instance by id.
     *
     * @param processInstanceId
     * @return
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/variables/get-variables/
     */
    @GetMapping(value = "/process-instance/{id}/variables", consumes = "application/json")
    Map<String, VariableValueDto> getProcessInstanceVariables(
        @PathVariable("id") String processInstanceId
    );

    /**
     * Queries for historic variable instances that fulfill the given
     * parameters.
     *
     * @param processInstanceId
     * @return
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/history/variable-instance/get-variable-instance-query/
     */
    @GetMapping(value = "/history/variable-instance", consumes = "application/json")
    List<HistoricVariableInstanceDto> getHistoryProcessInstanceVariables(
        @RequestParam("processInstanceId") String processInstanceId
    );

    /**
     * Retrieves an Activity Instance (Tree) for a given process instance by id.
     *
     * @param processInstanceId
     * @return
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/get-activity-instances/
     */
    @GetMapping(value = "/process-instance/{id}}/activity-instances", consumes = "application/json")
    ActivityInstanceDto getProcessInstanceActivityInstances(
        @PathVariable("id") String processInstanceId
    );

    /**
     * Queries for historic activity instances that fulfill the given
     * parameters.
     *
     * @param processInstanceId
     * @return
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/history/activity-instance/get-activity-instance-query/
     */
    @GetMapping(value = "/history/activity-instance", consumes = "application/json")
    List<HistoricActivityInstanceDto> getHistoryProcessInstanceActivityInstances(
        @RequestParam("processInstanceId") String processInstanceId
    );

    /**
     * Queries for the external tasks that fulfill given parameters. Parameters
     * may be static as well as dynamic runtime properties of executions.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/external-task/get-query/
     */
    @GetMapping(value = "/external-task", consumes = "application/json")
    List<TaskDto> getExternalTasks(
        @RequestParam("processInstanceId") String processInstanceId
    );

    /**
     * Queries for historic external task logs that fulfill the given
     * parameters.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/history/external-task-log/get-external-task-log-query/
     */
    @GetMapping(value = "/history/external-task-log", consumes = "application/json")
    List<HistoricExternalTaskLogDto> getHistoryExternalTaskLog(
        @RequestParam("processInstanceId") String processInstanceId
    );

    /**
     * Sets the number of retries left to execute external tasks by id
     * synchronously. If retries are set to 0, an incident is created.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/external-task/put-retries-sync/
     */
    @PutMapping(value = "/external-task/retries", consumes = "application/json")
    ResponseEntity<Void> setExternalTaskRetries(
        @RequestBody SetRetriesForExternalTasksDto request
    );

    /**
     * Retrieves the error details in the context of a running external task by
     * id.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/external-task/get-error-details/
     */
    @GetMapping(value = "/external-task/{activityId}/errorDetails", consumes = "application/json")
    String getExternalTaskErrorDetails(
        @PathVariable("activityId") String activityId
    );

    /**
     * Retrieves the corresponding error details of the passed historic external
     * task log by id.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/history/external-task-log/get-external-task-log-error-details/
     */
    @GetMapping(value = "/history/external-task-log/{logId}/error-details", consumes = "application/json")
    String getHistoryExternalTaskLogErrorDetails(
        @PathVariable("logId") String logId
    );

    /**
     * Queries for tasks that fulfill a given filter.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/task/get-query/
     */
    @GetMapping(value = "/task", consumes = "application/json")
    List<TaskDto> getTasks(
        @RequestParam("processInstanceId") String processInstanceId
    );

    /**
     * Queries for tasks that fulfill a given filter.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/task/get-query/
     */
    @GetMapping(value = "/task", consumes = "application/json")
    List<TaskDto> findTaskById(
        @RequestParam String processInstanceBusinessKey,
        @RequestParam String taskId
    );

    /**
     * Completes a task and updates process variables.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/task/post-complete/
     */
    @PostMapping(value = "/task/{id}/complete", consumes = "application/json")
    ResponseEntity<Void> completeTask(
        @PathVariable String id,
        CompleteTaskDto completeTask
    );

    /**
     * Queries for historic tasks that fulfill the given parameters.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/history/task/get-task-query/
     */
    @GetMapping(value = "/history/task", consumes = "application/json")
    List<HistoricTaskInstanceDto> getHistoryTasks(
        @RequestParam("processInstanceId") String processInstanceId
    );

    /**
     * Correlates a message to the process engine to either trigger a message
     * start event or an intermediate message catching event.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/message/
     */
    @PostMapping(value = "/message", consumes = "application/json")
    List<MessageCorrelationResultWithVariableDto> correlateMessage(CorrelationMessageDto correlationMessage);

    /**
     * Queries for the number of incidents that fulfill given parameters.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/incident/get-incident/
     */
    @GetMapping(value = "/incident/count", consumes = "application/json")
    CountResultDto countIncidents();

    /**
     * Queries for incidents that fulfill given parameters.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/incident/get-query/
     */
    @GetMapping(value = "/incident", consumes = "application/json")
	List<IncidentDto> getIncidents(
        @RequestParam(required = false) String incidentId,
        @RequestParam(required = false) String processDefinitionId,
        @RequestParam(required = false) String processInstanceId,
        @RequestParam(required = false) String executionId,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortOrder
    );


    /**
     * Queries for historic incidents that fulfill given parameters.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/history/incident/get-incident-query/
     */
    @GetMapping(value = "/history/incident", consumes = "application/json")
    List<HistoricIncidentDto> getHistoryIncidents(
        @RequestParam String processInstanceId
    );

    /**
     * Deletes a running process instance by id.
     *
     * @param processInstanceId
     * @return
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/delete/
     */
    @DeleteMapping(value = "/process-instance/{id}")
    ResponseEntity<Void> deleteProcessInstance(@PathVariable("id") String processInstanceId);

}
