package eu.opertusmundi.common.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.favorite.EnumFavoriteSortField;
import eu.opertusmundi.common.model.favorite.EnumFavoriteType;
import eu.opertusmundi.common.model.favorite.FavoriteAssetCommandDto;
import eu.opertusmundi.common.model.favorite.FavoriteCommandDto;
import eu.opertusmundi.common.model.favorite.FavoriteDto;
import eu.opertusmundi.common.model.favorite.FavoriteException;
import eu.opertusmundi.common.model.favorite.FavoriteMessageCode;
import eu.opertusmundi.common.model.favorite.FavoriteProviderCommandDto;
import eu.opertusmundi.common.repository.FavoriteRepository;

@Service
public class DefaultFavoriteService implements FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private CatalogueService catalogueService;

    @Override
    public PageResultDto<FavoriteDto> findAll(
        Integer userId, EnumFavoriteType type, int pageIndex, int pageSize, EnumFavoriteSortField orderBy, EnumSortingOrder order
    ) {
        final Direction   direction   = order == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;
        final PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(direction, orderBy.getValue()));

        final Page<FavoriteDto>          page    = favoriteRepository.findAll(userId, type, pageRequest).map(f -> f.toDto(true));
        final long                       count   = page.getTotalElements();
        final List<FavoriteDto>          records = page.getContent();
        final PageResultDto<FavoriteDto> result  = PageResultDto.of(pageIndex, pageSize, records, count);

        return result;
    }

    @Override
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
    public void removeFavorite(Integer accountId, UUID key) {
        this.favoriteRepository.delete(accountId, key);
    }

    private FavoriteDto addFavorite(FavoriteAssetCommandDto command) {
        final CatalogueFeature feature = this.catalogueService.findOneFeature(command.getPid());

        if (feature == null) {
            throw new FavoriteException(
                FavoriteMessageCode.CATALOGUE_ITEM_NOT_FOUND,
                String.format("Catalogue item not found [pid=%s]", command.getPid())
            );
        }

        command.setFeature(feature);

        return this.favoriteRepository.create(command);
    }

    private FavoriteDto addFavorite(FavoriteProviderCommandDto command) {
        return this.favoriteRepository.create(command);
    }

}
