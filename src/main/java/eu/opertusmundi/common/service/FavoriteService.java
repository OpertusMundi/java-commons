package eu.opertusmundi.common.service;

import java.util.UUID;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.favorite.EnumFavoriteSortField;
import eu.opertusmundi.common.model.favorite.EnumFavoriteType;
import eu.opertusmundi.common.model.favorite.FavoriteCommandDto;
import eu.opertusmundi.common.model.favorite.FavoriteDto;
import eu.opertusmundi.common.model.favorite.FavoriteException;

public interface FavoriteService {

    /**
     * Search user favorites
     *
     * @param userId
     * @param type
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    PageResultDto<FavoriteDto> findAll(
        Integer userId, EnumFavoriteType type, int pageIndex, int pageSize, EnumFavoriteSortField orderBy, EnumSortingOrder order
    );

    /**
     * Add new favorite
     *
     * If a favorite already exists, the existing record is returned
     *
     * @param command
     * @return
     * @throws FavoriteException if command type is not supported or catalogue item does not exist
     */
    FavoriteDto addFavorite(FavoriteCommandDto command) throws FavoriteException;

    /**
     * Remove existing favorite
     *
     * @param accountId
     * @param key
     */
    void removeFavorite(Integer accountId, UUID key);
}
