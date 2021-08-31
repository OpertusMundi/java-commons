package eu.opertusmundi.common.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.account.AccountAssetDto;
import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import eu.opertusmundi.common.model.account.ConsumerServiceException;
import eu.opertusmundi.common.model.account.ConsumerServiceMessageCode;
import eu.opertusmundi.common.model.asset.EnumConsumerAssetSortField;
import eu.opertusmundi.common.model.asset.EnumConsumerSubSortField;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.catalogue.client.EnumType;
import eu.opertusmundi.common.repository.AccountAssetRepository;
import eu.opertusmundi.common.repository.AccountSubscriptionRepository;

// TODO: Implement data filtering/sorting/pagination at the database level

@Service
public class DefaultConsumerAssetService implements ConsumerAssetService {

    @Autowired
    private AccountAssetRepository accountAssetRepository;

    @Autowired
    private AccountSubscriptionRepository accountSubscriptionRepository;

    @Autowired
    private CatalogueService catalogueService;

    @Override
    public PageResultDto<AccountAssetDto> findAllAssets(
            UUID userKey, EnumType type, int pageIndex, int pageSize, EnumConsumerAssetSortField orderBy, EnumSortingOrder order
    ) {
        List<AccountAssetDto> records = this.accountAssetRepository.findAllByUserKey(userKey).stream()
            .map(e -> e.toDto())
            .collect(Collectors.toList());

        final String[]               pid    = records.stream().map(a -> a.getAssetId()).distinct().toArray(String[]::new);
        final List<CatalogueItemDto> assets = this.catalogueService.findAllById(pid);

        // Add catalogue items to records
        records.forEach(r -> {
            final CatalogueItemDto item = assets.stream()
                .filter(a -> a.getId().equals(r.getAssetId()))
                .findFirst()
                .orElse(null);

            if(item == null) {
                throw new ConsumerServiceException(ConsumerServiceMessageCode.CATALOGUE_ITEM_NOT_FOUND, String.format(
                    "Catalogue item not found [userKey=%s, accountAsset=%d, assetPid=%s]" ,
                    userKey, r.getId(), r.getAssetId()
                ));
            }
            // Remove superfluous data
            item.setAutomatedMetadata(null);

            r.setItem(item);
        });

        // Filtering
        if (type != null) {
            records = records.stream().filter(r -> r.getItem().getType() == type).collect(Collectors.toList());
        }
        // Sorting
        records.sort((r1, r2) -> {
            switch(orderBy) {
                case ADDED_ON:
                    return r1.getAddedOn().compareTo(r2.getAddedOn());
                case PUBLISHER:
                    // TODO: Check if the publisher name is in sync with
                    // platform providers
                    return r1.getItem().getPublisherName().compareTo(r2.getItem().getPublisherName());
                case TITLE:
                    return r1.getItem().getTitle().compareTo(r2.getItem().getTitle());
                case UPDATE_ELIGIBILITY:
                    return r1.getUpdateEligibility().compareTo(r2.getUpdateEligibility());
            }
            return 0;
        });
        // Pagination
        final List<AccountAssetDto> items = records.stream()
            .skip(pageIndex * pageSize)
            .limit(pageSize)
            .collect(Collectors.toList());

        return PageResultDto.of(pageIndex, pageSize, items);
    }

    @Override
    public PageResultDto<AccountSubscriptionDto> findAllSubscriptions(
            UUID userKey, EnumSpatialDataServiceType type, int pageIndex, int pageSize, EnumConsumerSubSortField orderBy, EnumSortingOrder order
    ) {
        List<AccountSubscriptionDto> records = this.accountSubscriptionRepository.findAllByUserKeyForConsumer(userKey);

        final String[]               pid    = records.stream().map(a -> a.getServiceId()).distinct().toArray(String[]::new);
        final List<CatalogueItemDto> assets = this.catalogueService.findAllById(pid);

        // Add catalogue items to records
        records.forEach(r -> {
            final CatalogueItemDto item = assets.stream()
                .filter(a -> a.getId().equals(r.getServiceId()))
                .findFirst()
                .orElse(null);

            if(item == null) {
                throw new ConsumerServiceException(ConsumerServiceMessageCode.CATALOGUE_ITEM_NOT_FOUND, String.format(
                    "Catalogue item not found [userKey=%s, accountAsset=%d, assetPid=%s]" ,
                    userKey, r.getId(), r.getServiceId()
                ));
            }
            // Remove superfluous data
            item.setAutomatedMetadata(null);

            r.setItem(item);
        });

        // Filtering
        if (type != null) {
            records = records.stream().filter(r -> r.getItem().getSpatialDataServiceType() == type).collect(Collectors.toList());
        }
        // Sorting
        records.sort((r1, r2) -> {
            switch(orderBy) {
                case ADDED_ON:
                    return r1.getAddedOn().compareTo(r2.getAddedOn());
                case UPDATED_ON:
                    return r1.getUpdatedOn().compareTo(r2.getUpdatedOn());
                case PUBLISHER:
                    // TODO: Check if the publisher name is in sync with
                    // platform providers
                    return r1.getItem().getPublisherName().compareTo(r2.getItem().getPublisherName());
                case TITLE:
                    return r1.getItem().getTitle().compareTo(r2.getItem().getTitle());
            }
            return 0;
        });
        // Pagination
        final List<AccountSubscriptionDto> items = records.stream()
            .skip(pageIndex * pageSize)
            .limit(pageSize)
            .collect(Collectors.toList());

        return PageResultDto.of(pageIndex, pageSize, items);
    }

}
