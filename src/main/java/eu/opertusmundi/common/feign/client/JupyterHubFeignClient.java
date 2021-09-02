package eu.opertusmundi.common.feign.client;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import eu.opertusmundi.common.feign.client.config.JupyterHubFeignClientConfiguration;
import eu.opertusmundi.common.model.jupyter.server.GroupUsersCommandDto;
import eu.opertusmundi.common.model.jupyter.server.UserDto;
import eu.opertusmundi.common.model.jupyter.server.UserServerCommandDto;
import eu.opertusmundi.common.model.jupyter.server.UserTokenCommandDto;
import eu.opertusmundi.common.model.jupyter.server.UserTokenDto;
import eu.opertusmundi.common.model.jupyter.server.UserTokensDto;
import eu.opertusmundi.common.model.jupyter.server.VersionDto;

@ConditionalOnProperty(name = "opertusmundi.feign.jupyterhub.name")
@FeignClient(
    name = "${opertusmundi.feign.jupyterhub.name}",
    url = "${opertusmundi.feign.jupyterhub.url}",
    configuration = JupyterHubFeignClientConfiguration.class
)
public interface JupyterHubFeignClient 
{
    /**
     * Get version of REST API
     * @return
     */
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<VersionDto> getVersion();
    
    /**
     * List users of JupyterHub application
     * @return
     */
    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<UserDto>> listUsers();
    
    /**
     * Get user info
     * @param userName
     * @return
     */
    @GetMapping(value = "/users/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserDto> getUser(@PathVariable("name") String userName);
    
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
        @PathVariable("name") String groupName,
        @RequestBody GroupUsersCommandDto command
    );
    
    /**
     * List active tokens for a user
     * 
     * @param userName
     * @return
     */
    @GetMapping(value = "/users/{name}/tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserTokensDto> listTokensForUser(@PathVariable("name") String userName);
    
    /**
     * Create an API token for a user
     * 
     * @param userName
     * @param command
     * @return
     */
    @PostMapping(value = "/users/{name}/tokens", 
        produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserTokenDto> createTokenForUser(
        @PathVariable("name") String userName,
        @RequestBody UserTokenCommandDto command
    );
    
    /**
     * Start a user's server.
     * 
     * <p>The server starts asynchronously. When server is started, this is reflected on the user status
     * (see {@link JupyterHubFeignClient#getUser(String)}) on {@code servers[''].ready}
     * 
     * <p>Note: Attempting to start a server with an illegal profile (see {@code command.profileName})
     * will result in HTTP 5xx error.
     *    
     * @param userName
     * @param command
     * @return
     */
    @PostMapping(value = "/users/{name}/server", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> startServerForUser(
        @PathVariable("name") String userName,
        @RequestBody UserServerCommandDto command
    );
    
    /**
     * Stop a user's server.
     * 
     * <p>The server stops asynchronously. When stopped, this is reflected on the user status
     * (see {@link JupyterHubFeignClient#getUser(String)}) because the respective server object disappears.
     * @param userName
     * @return
     */
    @DeleteMapping(value = "/users/{name}/server")
    ResponseEntity<Void> stopServerForUser(@PathVariable("name") String userName);
}
