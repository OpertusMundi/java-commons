package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.AssetContractAnnexEntity;
import eu.opertusmundi.common.domain.ProviderAssetDraftEntity;
import eu.opertusmundi.common.model.asset.AssetContractAnnexCommandDto;
import eu.opertusmundi.common.model.asset.AssetContractAnnexDto;
import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.service.AssetDraftException;

@Repository
@Transactional(readOnly = true)
public interface AssetContractAnnexRepository extends JpaRepository<AssetContractAnnexEntity, Integer> {

    @Query("SELECT a FROM Account a WHERE a.key = :key")
    Optional<AccountEntity> findAccountByKey(UUID key);

    @Query("SELECT d FROM ProviderAssetDraft d WHERE d.key = :draftKey and d.account.key = :publisherKey")
    Optional<ProviderAssetDraftEntity> findDraftByPublisherAndKey(UUID publisherKey, UUID draftKey);

    @Query("SELECT r FROM AssetContractAnnex r WHERE r.fileName = :fileName and r.draftKey = :draftKey")
    Optional<AssetContractAnnexEntity> findOneByDraftKeyAndFileName(UUID draftKey, String fileName);

    @Query("SELECT r FROM AssetContractAnnex r WHERE r.key = :resourceKey and r.pid = :pid")
    Optional<AssetContractAnnexEntity> findOneByAssetPidAndResourceKey(String pid, String resourceKey);

    @Query("SELECT r FROM AssetContractAnnex r WHERE r.key = :resourceKey and r.draftKey = :draftKey")
    Optional<AssetContractAnnexEntity> findOneByDraftKeyAndResourceKey(UUID draftKey, String resourceKey);

    @Query("SELECT r FROM AssetContractAnnex r WHERE r.draftKey = :draftKey")
    List<AssetContractAnnexEntity> findAllAnnexesByDraftKey(UUID draftKey);

    @Query("SELECT r FROM AssetContractAnnex r WHERE r.pid = :pid")
    List<AssetContractAnnexEntity> findAllAnnexesByAssetPid(String pid);

    @Transactional(readOnly = false)
    default AssetContractAnnexDto update(AssetContractAnnexCommandDto command) throws AssetDraftException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getDraftKey(), "Expected a non-null draft key");
        Assert.notNull(command.getPublisherKey(), "Expected a non-null publisher key");
        Assert.hasText(command.getFileName(), "Expected a non-empty file name");

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

        AssetContractAnnexEntity resource = this.findOneByDraftKeyAndFileName(command.getDraftKey(), command.getFileName()).orElse(null);

        if(resource == null ) {
            resource = new AssetContractAnnexEntity(command.getDraftKey());
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
    default void linkDraftAnnexesToAsset(UUID draftKey, String pid) {
        final List<AssetContractAnnexEntity> annexes = this.findAllAnnexesByDraftKey(draftKey);

        annexes.forEach(r -> r.setPid(pid));
    }

    @Transactional(readOnly = false)
    default AssetContractAnnexDto delete(UUID draftKey, String annexKey) {
        Assert.notNull(draftKey, "Expected a non-null draft key");
        Assert.notNull(annexKey, "Expected a non-null resource key");

        final AssetContractAnnexEntity entity = this.findOneByDraftKeyAndResourceKey(draftKey, annexKey).orElse(null);

        if(entity == null) {
            throw new AssetDraftException(
                AssetMessageCode.CONTRACT_ANNEX_NOT_FOUND,
                String.format("Contract annex [%s] was not found", annexKey)
            );
        }

        final AssetContractAnnexDto resource = entity.toDto();

        this.delete(entity);

        return resource;
    }

}
