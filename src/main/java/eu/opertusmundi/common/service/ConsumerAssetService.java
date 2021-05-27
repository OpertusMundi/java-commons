package eu.opertusmundi.common.service;

import java.util.UUID;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.asset.EnumConsumerAssetSortField;
import eu.opertusmundi.common.model.catalogue.client.EnumType;
import eu.opertusmundi.common.model.dto.AccountAssetDto;
import eu.opertusmundi.common.model.dto.ConsumerServiceException;
import eu.opertusmundi.common.model.dto.EnumSortingOrder;

public interface ConsumerAssetService {

    default PageResultDto<AccountAssetDto> findAllAssets(
        UUID userKey, EnumType type, int pageIndex, int pageSize
    ) throws ConsumerServiceException {
        return this.findAllAssets(
            userKey, type, pageIndex, pageSize, EnumConsumerAssetSortField.ADDED_ON, EnumSortingOrder.DESC
        );
    }

    /**
     * Search purchased assets
     *
     * @param userKey
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     * @throws ConsumerServiceException if a catalogue item is not found
     */
    PageResultDto<AccountAssetDto> findAllAssets(
        UUID userKey, EnumType type, int pageIndex, int pageSize, EnumConsumerAssetSortField orderBy, EnumSortingOrder order
    ) throws ConsumerServiceException;

}
