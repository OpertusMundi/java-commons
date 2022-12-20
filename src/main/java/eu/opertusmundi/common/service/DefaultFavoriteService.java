package eu.opertusmundi.common.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.FavoriteAssetEntity;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.favorite.EnumAssetFavoriteAction;
import eu.opertusmundi.common.model.favorite.EnumFavoriteSortField;
import eu.opertusmundi.common.model.favorite.EnumFavoriteType;
import eu.opertusmundi.common.model.favorite.FavoriteAssetCommandDto;
import eu.opertusmundi.common.model.favorite.FavoriteAssetDto;
import eu.opertusmundi.common.model.favorite.FavoriteCommandDto;
import eu.opertusmundi.common.model.favorite.FavoriteDto;
import eu.opertusmundi.common.model.favorite.FavoriteException;
import eu.opertusmundi.common.model.favorite.FavoriteMessageCode;
import eu.opertusmundi.common.model.favorite.FavoriteProviderCommandDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.FavoriteRepository;

@Service
public class DefaultFavoriteService implements FavoriteService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private CatalogueService catalogueService;

    @Override
    public PageResultDto<FavoriteDto> findAll(
        Integer userId, EnumFavoriteType type, EnumAssetFavoriteAction action, int pageIndex, int pageSize, EnumFavoriteSortField orderBy, EnumSortingOrder order
    ) {
        final Direction   direction   = order == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;
        final PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(direction, orderBy.getValue()));

        final Page<FavoriteDto> page    = action == null
            ? favoriteRepository.findAll(userId, type, pageRequest).map(f -> f.toDto(true))
            : favoriteRepository.findAllAsset(userId, action, pageRequest).map(f -> f.toDto(true));
        final List<FavoriteDto> records = page.getContent();

        if (records.isEmpty()) {
            // Result is empty
            return PageResultDto.of(pageIndex, pageSize, records, 0);
        }

        // Set item details for asset favorites
        final String[] assetId = records.stream()
            .filter(r -> r.getType() == EnumFavoriteType.ASSET)
            .map(r -> ((FavoriteAssetDto) r).getAssetId())
            .toArray(String[]::new);

        if (assetId.length == 0) {
            // No asset favorites were found
            return PageResultDto.of(pageIndex, pageSize, records, page.getTotalElements());
        }

        final List<CatalogueItemDetailsDto> items          = this.catalogueService.findAllPublishedById(assetId);
        final List<FavoriteDto>             updatedRecords = records.stream()
            .map(r -> {
                switch (r.getType()) {
                    case ASSET : {
                        final FavoriteAssetDto favoriteAsset = (FavoriteAssetDto) r;
                        final CatalogueItemDetailsDto item   = items.stream()
                            .filter(i -> i.getId().equals(favoriteAsset.getAssetId()))
                            .findFirst()
                            .orElse(null);

                        if (item == null) {
                            return null;
                        }
                        favoriteAsset.setAsset(item);
                        return favoriteAsset;
                    }
                    default :
                        return r;
                }
            })
            .filter(f -> f != null)
            .collect(Collectors.toList());

        // Adjust count and compose result
        final long                       count  = page.getTotalElements() - records.size() + updatedRecords.size();
        final PageResultDto<FavoriteDto> result = PageResultDto.of(pageIndex, pageSize, updatedRecords, count);

        return result;
    }

    @Override
    @Transactional
    public FavoriteDto addFavorite(FavoriteCommandDto command) {
        switch (command.getType()) {
            case ASSET :
                return this.addFavorite((FavoriteAssetCommandDto) command);
            case PROVIDER :
                return this.addFavorite((FavoriteProviderCommandDto) command);
            default :
                throw new FavoriteException(
                    FavoriteMessageCode.FAVORITE_TYPE_NOT_SUPPORTED,
                    String.format("Favorite type not supported [type=%s]", command.getType())
                );
        }
    }

    @Override
    @Transactional
    public void removeFavorite(Integer accountId, UUID key) {
        final var favorite = this.favoriteRepository.findOneByKey(key).orElse(null);
        if (favorite == null) {
            return;
        }
        this.favoriteRepository.delete(accountId, key);
        if (favorite instanceof final FavoriteAssetEntity assetFavorite &&
            assetFavorite.getAction() == EnumAssetFavoriteAction.PURCHASE
        ) {
            this.refreshProviderSaleLeadCount(assetFavorite.getAssetProvider());
        }
    }

    private FavoriteDto addFavorite(FavoriteAssetCommandDto command) {
        final var action = command.getAction();
        final var items  = this.catalogueService.findAllPublishedById(new String[]{command.getPid()});

        if (items.isEmpty()) {
            throw new FavoriteException(
                FavoriteMessageCode.CATALOGUE_ITEM_NOT_FOUND,
                String.format("Catalogue item not found [pid=%s]", command.getPid())
            );
        }
        command.setItem(items.get(0));

        final FavoriteAssetDto result = (FavoriteAssetDto) this.favoriteRepository.create(command);
        result.setAsset(items.get(0));

        if (action == EnumAssetFavoriteAction.PURCHASE) {
            final var providerId = items.get(0).getPublisher().getId();
            this.refreshProviderSaleLeadCount(providerId);
        }

        return result;
    }

    private FavoriteDto addFavorite(FavoriteProviderCommandDto command) {
        return this.favoriteRepository.create(command);
    }

    private void refreshProviderSaleLeadCount(int id) {
        final var provider = this.accountRepository.findById(id).get();
        final var count    = this.favoriteRepository.countAssetFavoriteByActionAndProvider(EnumAssetFavoriteAction.PURCHASE, id);

        provider.getProvider().setSaleLeadCount(count);
        this.accountRepository.saveAndFlush(provider);
    }
}
