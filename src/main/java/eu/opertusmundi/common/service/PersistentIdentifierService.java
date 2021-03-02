package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.pid.PersistentIdentifierServiceException;

public interface PersistentIdentifierService {

    /**
     * Register provider to persistent identifier service
     * 
     * @param name
     * @param nameSpace
     * @return
     */
    Integer registerUser(String name) throws PersistentIdentifierServiceException;

    /**
     * Register asset type
     * 
     * @param id
     * @param description
     */
    void registerAssetType(String id, String description) throws PersistentIdentifierServiceException;

    /**
     * Register asset
     * 
     * @param localId
     * @param ownerId
     * @param assetType
     * @return
     */
    default String registerAsset(String localId, Integer ownerId, String assetType) throws PersistentIdentifierServiceException {
        return this.registerAsset(localId, ownerId, assetType, null);
    }

    /**
     * Register asset
     * 
     * @param localId
     * @param ownerId
     * @param assetType
     * @param description
     * @return
     */
    String registerAsset(String localId, Integer ownerId, String assetType, String description) throws PersistentIdentifierServiceException;

}
