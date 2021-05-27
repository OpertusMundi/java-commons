package eu.opertusmundi.common.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountAssetEntity;

@Repository
@Transactional(readOnly = true)
public interface AccountAssetRepository extends JpaRepository<AccountAssetEntity, Integer> {

    @Query("SELECT a FROM AccountAsset a WHERE a.consumer.key = :userKey")
    List<AccountAssetEntity> findAllByUserKey(@Param("userKey") UUID userKey);

    @Query("SELECT a FROM AccountAsset a WHERE a.consumer.key = :userKey and a.asset = :assetId")
    List<AccountAssetEntity> findAllByUserKeyAndAssetId(@Param("userKey") UUID userKey, @Param("assetId") String assetId);

}
