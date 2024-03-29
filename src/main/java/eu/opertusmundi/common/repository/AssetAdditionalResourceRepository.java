package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.AssetAdditionalResourceEntity;
import eu.opertusmundi.common.domain.ProviderAssetDraftEntity;
import eu.opertusmundi.common.model.asset.AssetAdditionalResourceCommandDto;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.service.AssetDraftException;

@Repository
@Transactional(readOnly = true)
public interface AssetAdditionalResourceRepository extends JpaRepository<AssetAdditionalResourceEntity, Integer> {


    @Query("SELECT a FROM Account a WHERE a.key = :key")
    Optional<AccountEntity> findAccountByKey(@Param("key") UUID key);

    @Query("SELECT d FROM ProviderAssetDraft d WHERE d.key = :draftKey and d.account.key = :publisherKey")
    Optional<ProviderAssetDraftEntity> findDraftByPublisherAndKey(
        @Param("publisherKey") UUID publisherKey, @Param("draftKey") UUID draftKey
    );

    @Query("SELECT r FROM AssetAdditionalResource r WHERE r.fileName = :fileName and r.draftKey = :draftKey")
    Optional<AssetAdditionalResourceEntity> findOneByDraftKeyAndFileName(
        @Param("draftKey") UUID draftKey, @Param("fileName") String fileName
    );

    @Query("SELECT r FROM AssetAdditionalResource r WHERE r.key = :resourceKey and r.pid = :pid")
    Optional<AssetAdditionalResourceEntity> findOneByAssetPidAndResourceKey(
        @Param("pid") String pid, @Param("resourceKey") String resourceKey
    );

    @Query("SELECT r FROM AssetAdditionalResource r WHERE r.key = :resourceKey and r.draftKey = :draftKey")
    Optional<AssetAdditionalResourceEntity> findOneByDraftKeyAndResourceKey(
        @Param("draftKey") UUID draftKey, @Param("resourceKey") String resourceKey
    );

    @Query("SELECT r FROM AssetAdditionalResource r WHERE r.draftKey = :key")
    List<AssetAdditionalResourceEntity> findAllResourcesByDraftKey(@Param("key") UUID draftKey);

    @Query("SELECT r FROM AssetAdditionalResource r WHERE r.pid = :pid")
    List<AssetAdditionalResourceEntity> findAllResourcesByAssetPid(@Param("pid") String pid);

    @Transactional(readOnly = false)
    default AssetFileAdditionalResourceDto update(AssetAdditionalResourceCommandDto command) throws AssetDraftException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getDraftKey(), "Expected a non-null draft key");
        Assert.notNull(command.getPublisherKey(), "Expected a non-null publisher key");
        Assert.isTrue(!StringUtils.isBlank(command.getFileName()), "Expected a non-empty file name");

        // Check draft
        final ProviderAssetDraftEntity draft = this.findDraftByPublisherAndKey(command.getPublisherKey(), command.getDraftKey()).orElse(null);

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }

        // Check status
        if (draft.getStatus() != EnumProviderAssetDraftStatus.DRAFT) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status to bue [DRAFT]. Found [%s]", draft.getStatus())
            );
        }

        // Check provider
        final AccountEntity account = this.findAccountByKey(command.getPublisherKey()).orElse(null);

        if (account == null) {
            throw new AssetDraftException(AssetMessageCode.PROVIDER_NOT_FOUND);
        }

        AssetAdditionalResourceEntity resource = this.findOneByDraftKeyAndFileName(command.getDraftKey(), command.getFileName()).orElse(null);

        if(resource == null ) {
            resource = new AssetAdditionalResourceEntity(command.getDraftKey());
        }

        resource.setAccount(account);
        resource.setCreatedOn(ZonedDateTime.now());
        resource.setDescription(command.getDescription());
        resource.setFileName(command.getFileName());
        resource.setSize(command.getSize());

        this.saveAndFlush(resource);

        return resource.toDto();
    }

    @Transactional(readOnly = false)
    default void linkDraftResourcesToAsset(UUID draftKey, String pid) {
        final List<AssetAdditionalResourceEntity> resources = this.findAllResourcesByDraftKey(draftKey);

        resources.forEach(r -> r.setPid(pid));
    }

    @Transactional(readOnly = false)
    default AssetFileAdditionalResourceDto delete(UUID draftKey, String resourceKey) {
        Assert.notNull(draftKey, "Expected a non-null draft key");
        Assert.notNull(resourceKey, "Expected a non-null resource key");

        final AssetAdditionalResourceEntity entity = this.findOneByDraftKeyAndResourceKey(draftKey, resourceKey).orElse(null);

        if(entity == null) {
            throw new AssetDraftException(
                AssetMessageCode.ADDITIONAL_RESOURCE_NOT_FOUND,
                String.format("Resource [%s] was not found", resourceKey)
            );
        }

        final AssetFileAdditionalResourceDto resource = entity.toDto();

        this.delete(entity);

        return resource;
    }

    @Transactional(readOnly = false)
    default void deleteAll(UUID draftKey) {
        Assert.notNull(draftKey, "Expected a non-null draft key");

        final List<AssetAdditionalResourceEntity> resources = this.findAllResourcesByDraftKey(draftKey);

        resources.stream().forEach(r -> this.delete(r));
    }

}
