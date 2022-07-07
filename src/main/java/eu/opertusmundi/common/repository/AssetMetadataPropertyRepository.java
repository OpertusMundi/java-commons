package eu.opertusmundi.common.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AssetMetadataPropertyEntity;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;

@Repository
@Transactional(readOnly = true)
public interface AssetMetadataPropertyRepository extends JpaRepository<AssetMetadataPropertyEntity, Integer> {

    @Query("SELECT p FROM AssetMetadataProperty p WHERE p.assetType = :assetType")
    List<AssetMetadataPropertyEntity> findAllByAssetType(EnumAssetType assetType);

    @Query("SELECT p FROM AssetMetadataProperty p WHERE p.assetType = :assetType and p.name = :name")
    Optional<AssetMetadataPropertyEntity> findOneByAssetTypeAndName(EnumAssetType assetType, String name);

}