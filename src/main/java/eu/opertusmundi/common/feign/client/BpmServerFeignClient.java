package eu.opertusmundi.common.feign.client;

import java.util.List;

import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto;
import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultWithVariableDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceWithVariablesDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
     * Retrieves the list of process definitions
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
     * Get Instance
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/get/
     */
    @PostMapping(value = "/process-instance", consumes = "application/json")
    List<ProcessInstanceDto> getInstance(
        @PathVariable("businessKey") String businessKey
    );

    /**
     * Starts process instance by key
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/process-definition/post-start-process-instance/
     */
    @PostMapping(value = "/process-definition/key/{key}/start", consumes = "application/json")
    ProcessInstanceWithVariablesDto startProcessByKey(
        @PathVariable("key") String processDefinitionKey,
        StartProcessInstanceDto startProcessInstance
    );

    /**
     * Queries for tasks that fulfill a given filter.
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/task/get-query/
     */
    @GetMapping(value = "/task", consumes = "application/json")
    List<TaskDto> findInstanceTaskById(
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
     * Correlates message
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/message/
     */
    @PostMapping(value = "/message", consumes = "application/json")
    List<MessageCorrelationResultWithVariableDto> correlateMessage(CorrelationMessageDto correlationMessage);

}
