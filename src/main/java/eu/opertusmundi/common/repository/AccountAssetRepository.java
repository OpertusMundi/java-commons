package eu.opertusmundi.common.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountAssetEntity;

@Repository
@Transactional(readOnly = true)
public interface AccountAssetRepository extends JpaRepository<AccountAssetEntity, Integer> {

    @Query("SELECT a FROM AccountAsset a WHERE a.consumer.key = :userKey")
    List<AccountAssetEntity> findAllByUserKey(UUID userKey);

    @Query("SELECT a FROM AccountAsset a WHERE a.consumer.key = :userKey and a.asset = :assetId")
    List<AccountAssetEntity> findAllByUserKeyAndAssetId(UUID userKey, String assetId);

    @Query("SELECT a FROM AccountAsset a WHERE a.consumer.key = :userKey and a.provider.key = :providerKey")
    List<AccountAssetEntity> findAllByUserKeyAndProviderKey(UUID userKey, UUID providerKey);

    default boolean checkOwnershipByAsset(UUID userKey, String assetId) {
        final List<AccountAssetEntity> ownedAssets = this.findAllByUserKeyAndAssetId(userKey, assetId);

        return !ownedAssets.isEmpty();
    }

    default boolean checkPurchaseByProvider(UUID userKey, UUID providerKey) {
        final List<AccountAssetEntity> ownedAssets = this.findAllByUserKeyAndProviderKey(userKey, providerKey);

        return !ownedAssets.isEmpty();
    }

}
