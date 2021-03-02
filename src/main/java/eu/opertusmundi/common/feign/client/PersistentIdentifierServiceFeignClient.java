package eu.opertusmundi.common.feign.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.feign.client.config.PersistentIdentifierServiceFeignClientConfiguration;
import eu.opertusmundi.common.model.pid.AssetRegistrationDto;
import eu.opertusmundi.common.model.pid.AssetTypeRegistrationDto;
import eu.opertusmundi.common.model.pid.RegisterAssetCommandDto;
import eu.opertusmundi.common.model.pid.RegisterAssetTypeCommandDto;
import eu.opertusmundi.common.model.pid.RegisterUserCommandDto;
import eu.opertusmundi.common.model.pid.UserRegistrationDto;

@FeignClient(
    name = "${opertusmundi.feign.persistent-identifier-service.name}",
    url = "${opertusmundi.feign.persistent-identifier-service.url}",
    configuration = PersistentIdentifierServiceFeignClientConfiguration.class
)
public interface PersistentIdentifierServiceFeignClient {

    /**
     * Register user
     * 
     * @param command
     * @return
     */
    @PostMapping(value = "/users/register", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserRegistrationDto> registerUser(@RequestBody RegisterUserCommandDto command);
    
    /**
     * Get user by id
     * 
     * @param id
     * @return
     */
    @GetMapping(value = "/users/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserRegistrationDto> findUserById(@PathVariable("id") Integer id);

    /**
     * Register asset type
     * 
     * @param command
     * @return
     */
    @PostMapping(value = "/asset_types/register", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AssetTypeRegistrationDto> registerAssetType(@RequestBody RegisterAssetTypeCommandDto command);
    
    /**
     * Get all asset types
     * 
     * @return
     */
    @GetMapping(value = "/asset_types", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<AssetTypeRegistrationDto>> findAllAssetTypes();
    
    /**
     * Get asset type by id
     * 
     * @param id
     * @return
     */
    @GetMapping(value = "/asset_types/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AssetTypeRegistrationDto> findAssetTypeById(@PathVariable("id") String id);
    
    /**
     * Register asset
     * 
     * @param command
     * @return
     */
    @PostMapping(value = "/assets/register", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AssetRegistrationDto> registerAsset(@RequestBody RegisterAssetCommandDto command);
    
    /**
     * Resolve local identifier to topio PID
     * 
     * @param command
     * @return
     */
    @GetMapping(value = "/assets/topio_id", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<JsonNode> resolveLocalId(
        @RequestParam("local_id") String localId,
        @RequestParam("owner_id") Integer ownerId,
        @RequestParam("asset_type") String assetType
   );
   
}
