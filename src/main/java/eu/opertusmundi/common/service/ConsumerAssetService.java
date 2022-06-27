package eu.opertusmundi.common.service;

import java.util.UUID;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.account.AccountAssetDto;
import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import eu.opertusmundi.common.model.account.ConsumerServiceException;
import eu.opertusmundi.common.model.asset.EnumConsumerAssetSortField;
import eu.opertusmundi.common.model.asset.EnumConsumerSubSortField;
import eu.opertusmundi.common.model.asset.FileResourceDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.file.CopyToDriveCommandDto;
import eu.opertusmundi.common.model.file.CopyToDriveResultDto;

public interface ConsumerAssetService {

    default PageResultDto<AccountAssetDto> findAllAssets(
        UUID userKey, EnumAssetType type, int pageIndex, int pageSize
    ) throws ConsumerServiceException {
        return this.findAllAssets(
            userKey, type, pageIndex, pageSize, EnumConsumerAssetSortField.ADDED_ON, EnumSortingOrder.DESC
        );
    }

    /**
     * Search purchased assets
     *
     * @param userKey
     * @param type
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     * @throws ConsumerServiceException if a catalogue item is not found
     */
    PageResultDto<AccountAssetDto> findAllAssets(
        UUID userKey, EnumAssetType type, int pageIndex, int pageSize, EnumConsumerAssetSortField orderBy, EnumSortingOrder order
    ) throws ConsumerServiceException;

    /**
     * Search registered subscriptions
     *
     * @param userKey
     * @param type
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     * @throws ConsumerServiceException if a catalogue item is not found
     */
    PageResultDto<AccountSubscriptionDto> findAllSubscriptions(
        UUID userKey, EnumSpatialDataServiceType type, int pageIndex, int pageSize, EnumConsumerSubSortField orderBy, EnumSortingOrder order
    ) throws ConsumerServiceException;

    /**
     * Get a subscription
     *
     * @param userKey
     * @param subscriptionKey
     * @return
     */
    AccountSubscriptionDto findSubscription(UUID userKey, UUID subscriptionKey);

    /**
     * Cancel active subscription
     *
     * @param userKey
     * @param subscriptionKey
     */
    void cancelSubscription(UUID userKey, UUID subscriptionKey);

    /**
     * Resolve the path of a resource for an asset, purchased by the specified user
     *
     * @param userKey
     * @param pid
     * @param resourceKey
     * @return
     * @throws ServiceException
     */
    FileResourceDto resolveResourcePath(UUID userKey, String pid, String resourceKey) throws ServiceException;

    /**
     * Copy asset resource to Topio drive
     *
     * @param command
     * @return
     * @throws ServiceException
     */
    CopyToDriveResultDto copyToDrive(CopyToDriveCommandDto command) throws ServiceException;

}
