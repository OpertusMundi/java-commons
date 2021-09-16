package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.FavoriteAssetEntity;
import eu.opertusmundi.common.domain.FavoriteEntity;
import eu.opertusmundi.common.domain.FavoriteProviderEntity;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.favorite.EnumFavoriteType;
import eu.opertusmundi.common.model.favorite.FavoriteAssetCommandDto;
import eu.opertusmundi.common.model.favorite.FavoriteDto;
import eu.opertusmundi.common.model.favorite.FavoriteException;
import eu.opertusmundi.common.model.favorite.FavoriteMessageCode;
import eu.opertusmundi.common.model.favorite.FavoriteProviderCommandDto;
import io.jsonwebtoken.lang.Assert;

@Repository
@Transactional(readOnly = true)
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Integer> {

    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<AccountEntity> findAccountById(Integer id);

    @Query("SELECT a FROM Account a WHERE a.key = :key")
    Optional<AccountEntity> findAccountByKey(UUID key);

    @Query("SELECT f FROM Favorite f WHERE (f.account.id = :accountId) and (:type is null or f.type = :type)")
    Page<FavoriteEntity> findAll(Integer accountId, EnumFavoriteType type, Pageable page);

    @Query("SELECT f FROM FavoriteAsset f WHERE f.account.id = :accountId")
    Page<FavoriteAssetEntity> findAllAsset(Integer accountId, Pageable page);

    @Query("SELECT f FROM FavoriteProvider f WHERE f.account.id = :accountId")
    Page<FavoriteProviderEntity> findAllProvider(Integer accountId, Pageable page);

    @Query("SELECT f FROM FavoriteAsset f WHERE f.account.id = :accountId and f.assetId = :assetId")
    Optional<FavoriteAssetEntity> findOneAsset(Integer accountId, String assetId);

    @Query("SELECT f FROM FavoriteProvider f WHERE f.account.id = :accountId and f.provider.key = :providerKey")
    Optional<FavoriteProviderEntity> findOneProvider(Integer accountId, UUID providerKey);

    @Transactional(readOnly = false)
    default FavoriteDto create(FavoriteAssetCommandDto command) {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getFeature(), "Expected a non-null feature");

        final AccountEntity    owner    = this.findAccountById(command.getUserId()).orElse(null);
        final CatalogueFeature feature  = command.getFeature();
        FavoriteAssetEntity    favorite = this.findOneAsset(command.getUserId(), command.getPid()).orElse(null);

        if(favorite != null) {
            return favorite.toDto(true);
        }

        favorite = new FavoriteAssetEntity();

        favorite.setAccount(owner);
        favorite.setAssetId(feature.getId());
        favorite.setAssetVersion(feature.getProperties().getVersion());
        favorite.setCreatedOn(ZonedDateTime.now());
        favorite.setKey(UUID.randomUUID());
        favorite.setTitle(feature.getProperties().getTitle());

        return this.save(favorite).toDto(true);
    }

    @Transactional(readOnly = false)
    default FavoriteDto create(FavoriteProviderCommandDto command) throws FavoriteException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getPublisherKey(), "Expected a non-null publisher key");

        final AccountEntity    owner    = this.findAccountById(command.getUserId()).orElse(null);
        final AccountEntity    provider = this.findAccountByKey(command.getPublisherKey()).orElse(null);
        FavoriteProviderEntity favorite = this.findOneProvider(command.getUserId(), command.getPublisherKey()).orElse(null);

        if (provider == null) {
            throw new FavoriteException(FavoriteMessageCode.PROVIDER_NOT_FOUND, "Provider not found");
        }

        if(favorite != null) {
            return favorite.toDto(true);
        }

        favorite = new FavoriteProviderEntity();

        favorite.setAccount(owner);
        favorite.setCreatedOn(ZonedDateTime.now());
        favorite.setKey(UUID.randomUUID());
        favorite.setProvider(provider);
        favorite.setTitle(provider.getProvider().getName());

        return this.save(favorite).toDto(true);
    }

    @Modifying
    @Transactional(readOnly = false)
    @Query("DELETE Favorite f WHERE f.account.id = :accountId and f.key = :key")
    int delete(Integer accountId, UUID key);

}
