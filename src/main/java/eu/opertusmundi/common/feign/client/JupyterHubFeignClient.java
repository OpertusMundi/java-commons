package eu.opertusmundi.common.feign.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import eu.opertusmundi.common.feign.client.config.JupyterHubFeignClientConfiguration;
import eu.opertusmundi.common.model.jupyter.server.GroupUsersCommandDto;

@ConditionalOnProperty(name = "opertusmundi.feign.jupyter-hub.name")
@FeignClient(
    name = "${opertusmundi.feign.jupyter-hub.name}",
    url = "${opertusmundi.feign.jupyter-hub.url}",
    configuration = JupyterHubFeignClientConfiguration.class
)
public interface JupyterHubFeignClient {

    /**
     * Add users to a group
     *
     * @param apiKey
     * @param groupName
     * @param command
     * @return
     */
    @PostMapping(value = "/groups/{name}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> addUsersToGroup(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String apiKey,
        @PathVariable("name") String groupName,
        @RequestBody GroupUsersCommandDto command
    );

    /**
     * Remove users from a group
     *
     * @param apiKey
     * @param groupName
     * @param command
     * @return
     */
    @DeleteMapping(value = "/groups/{name}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> removeUsersFromGroup(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String apiKey,
        @PathVariable("name") String groupName,
        @RequestBody GroupUsersCommandDto command
    );

}
