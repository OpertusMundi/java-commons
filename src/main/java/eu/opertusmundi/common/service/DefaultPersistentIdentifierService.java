package eu.opertusmundi.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.slugify.Slugify;

import eu.opertusmundi.common.feign.client.PersistentIdentifierServiceFeignClient;
import eu.opertusmundi.common.model.pid.AssetRegistrationDto;
import eu.opertusmundi.common.model.pid.AssetTypeRegistrationDto;
import eu.opertusmundi.common.model.pid.PersistentIdentifierServiceException;
import eu.opertusmundi.common.model.pid.PersistentIdentifierServiceMessageCode;
import eu.opertusmundi.common.model.pid.RegisterAssetCommandDto;
import eu.opertusmundi.common.model.pid.RegisterAssetTypeCommandDto;
import eu.opertusmundi.common.model.pid.RegisterUserCommandDto;
import eu.opertusmundi.common.model.pid.UserRegistrationDto;
import feign.FeignException;

@Service
public class DefaultPersistentIdentifierService implements PersistentIdentifierService {

    private static final Logger logger = LoggerFactory.getLogger(PersistentIdentifierService.class);
    
    @Autowired
    private ObjectProvider<PersistentIdentifierServiceFeignClient> pidClient;
    
    public Integer registerUser(String name) throws PersistentIdentifierServiceException {
        try {
            final Slugify s = new Slugify()
                .withTransliterator(true)
                .withLowerCase(true);
                
            final String userNamespace = s.slugify(name);
            
            final RegisterUserCommandDto command = RegisterUserCommandDto.builder()
                .name(name)
                .userNamespace(userNamespace)
                .build();
                
            final ResponseEntity<UserRegistrationDto> e = pidClient.getObject().registerUser(command);

            return e.getBody().getId();           
        } catch (final Exception ex) {
            logger.error("[Persistent Identifier Service] Operation has failed", ex);

            throw new PersistentIdentifierServiceException(
                PersistentIdentifierServiceMessageCode.USER_REGISTRATION,
                "Failed to register user", ex
            );
        }
    }

    public void registerAssetType(String id, String description) {
        try {               
            final ResponseEntity<AssetTypeRegistrationDto> e = pidClient.getObject().findAssetTypeById(id);

            if (e.getBody() != null) {
                // Asset type already registered
                return;
            }
        } catch (final Exception ex) {
            // Failed to query service for asset type 
            logger.error("[Persistent Identifier Service] Operation has failed", ex);

            throw new PersistentIdentifierServiceException(
                PersistentIdentifierServiceMessageCode.ASSET_TYPE_QUERY,
                "Failed to query asset type", ex
            );
        }
        
        try {
            final RegisterAssetTypeCommandDto command = RegisterAssetTypeCommandDto.builder()
                .id(id)
                .description(description)
                .build();
                
            pidClient.getObject().registerAssetType(command);         
        } catch (final Exception ex) {
            logger.error("[Persistent Identifier Service] Operation has failed", ex);

            throw new PersistentIdentifierServiceException(
                PersistentIdentifierServiceMessageCode.ASSET_TYPE_REGISTRATION,
                String.format("Failed to register asset type [%s]", id), ex
            );
        }
    }

    public String registerAsset(String localId, Integer ownerId, String assetType, String description) {
        try {           
            final ResponseEntity<JsonNode> e = pidClient.getObject().resolveLocalId(localId, ownerId, assetType);

            // Asset is already registered
            return e.getBody().asText();
        } catch (final FeignException fex) {
            if (fex.status() != HttpStatus.NOT_FOUND.value()) {
                // Failed to query service for asset registration 
                logger.error("[Persistent Identifier Service] Operation has failed", fex);

                throw new PersistentIdentifierServiceException(
                    PersistentIdentifierServiceMessageCode.ASSET_QUERY,
                    "Failed to query asset registration", fex
                );
            }
        } catch (final Exception ex) {
            // Failed to query service for asset registration 
            logger.error("[Persistent Identifier Service] Operation has failed", ex);

            throw new PersistentIdentifierServiceException(
                PersistentIdentifierServiceMessageCode.ASSET_QUERY,
                "Failed to query asset registration", ex
            );
        }
        
        try {
            final RegisterAssetCommandDto command = RegisterAssetCommandDto.builder()
                .localId(localId)
                .ownerId(ownerId)
                .assetType(assetType)
                .description(description)
                .build();
                
            final ResponseEntity<AssetRegistrationDto> e = pidClient.getObject().registerAsset(command);
            
            return e.getBody().getTopioId();
        } catch (final Exception ex) {
            logger.error("[Persistent Identifier Service] Operation has failed", ex);

            throw new PersistentIdentifierServiceException(
                PersistentIdentifierServiceMessageCode.ASSET_REGISTRATION,
                String.format("Failed to register asset [%s]", localId), ex
            );
        }
    }
    
}