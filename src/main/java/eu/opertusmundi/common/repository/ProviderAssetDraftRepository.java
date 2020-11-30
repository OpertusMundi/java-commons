package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.ProviderAssetDraftEntity;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.service.AssetDraftException;

@Repository
@Transactional(readOnly = true)
public interface ProviderAssetDraftRepository extends JpaRepository<ProviderAssetDraftEntity, Integer> {

    @Query("SELECT a FROM Account a WHERE a.key = :key")
    Optional<AccountEntity> findAccountByKey(@Param("key") UUID key);

    @Query("SELECT a FROM ProviderAssetDraft a WHERE "
           + "(a.status in :status or :status is null) and "
           + "(a.account.key = :publisherKey or :publisherKey is null)"
    )
    Page<ProviderAssetDraftEntity> findAllByPublisherAndStatus(
        @Param("publisherKey") UUID publisherKey, @Param("status") Set<EnumProviderAssetDraftStatus> status, Pageable pageable
    );

    @Query("SELECT a FROM ProviderAssetDraft a WHERE a.account.key = :publisherKey")
    Page<ProviderAssetDraftEntity> findAllByPublisher(
        @Param("publisherKey") UUID publisherKey, Pageable pageable
    );

    @Query("SELECT a FROM ProviderAssetDraft a WHERE a.status in :status")
    Page<ProviderAssetDraftEntity> findAllByStatus(
        @Param("status") Set<EnumProviderAssetDraftStatus> status, Pageable pageable
    );

    @Query("SELECT a FROM ProviderAssetDraft a WHERE a.key = :key and a.account.key = :publisherKey")
    Optional<ProviderAssetDraftEntity> findOneByPublisherAndKey(
        @Param("publisherKey") UUID publisherKey, @Param("key") UUID assetKey
    );

    @Transactional(readOnly = false)
    default AssetDraftDto update(CatalogueItemCommandDto command) throws AssetDraftException {
        return this.update(command, EnumProviderAssetDraftStatus.DRAFT, null, null);
    }

    @Transactional(readOnly = false)
    default AssetDraftDto update(
        CatalogueItemCommandDto command,
        EnumProviderAssetDraftStatus status,
        String processDefinition,
        String processInstance
    ) throws AssetDraftException {
        Assert.notNull(command, "Expected a non-null command");

        Assert.isTrue(
            status == EnumProviderAssetDraftStatus.DRAFT || status == EnumProviderAssetDraftStatus.SUBMITTED,
            "Expected status in [DRAFT, SUBMITTED]"
        );

        final ZonedDateTime now = ZonedDateTime.now();

        // Check provider
        final AccountEntity account = this.findAccountByKey(command.getPublisherKey()).orElse(null);

        if (account == null) {
            throw new AssetDraftException(AssetMessageCode.PROVIDER_NOT_FOUND);
        }

        // Check draft
        ProviderAssetDraftEntity draft = null;

        if (command.getAssetKey() != null) {
            draft = this.findOneByPublisherAndKey(command.getPublisherKey(), command.getAssetKey()).orElse(null);

            if (draft == null) {
                throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
            }
        } else {
            draft = new ProviderAssetDraftEntity();
            draft.setAssetDraft(draft.getKey());
        }

        if (draft.getStatus() != EnumProviderAssetDraftStatus.DRAFT) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status is [%s]. Found [%s]", status, draft.getStatus())
            );
        }

        draft.setAccount(account);
        draft.setCommand(command);
        draft.setIngested(command.isIngested());
        draft.setModifiedOn(now);
        draft.setProcessDefinition(processDefinition);
        draft.setProcessInstance(processInstance);
        draft.setSource(command.getSource());
        draft.setStatus(status);
        draft.setTitle(command.getTitle());
        draft.setVersion(command.getVersion());

        this.saveAndFlush(draft);

        return draft.toDto();
    }

    @Transactional(readOnly = false)
    default void updateStatus(UUID publisherKey, UUID draftKey, EnumProviderAssetDraftStatus status) throws AssetDraftException {
        final ProviderAssetDraftEntity draft = this.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null);

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }

        draft.setStatus(status);
    }

    @Transactional(readOnly = false)
    default void reject(UUID publisherKey, UUID draftKey, String reason, EnumProviderAssetDraftStatus status) throws AssetDraftException {
        final ProviderAssetDraftEntity draft = this.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null);

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }

        switch (status) {
            case HELPDESK_REJECTED :
                draft.setHelpdeskRejectionReason(reason);
                break;
            case PROVIDER_REJECTED :
                draft.setProviderRejectionReason(reason);
                break;
            default :
                throw new AssetDraftException(
                    AssetMessageCode.INVALID_STATE,
                    String.format("Expected status in [HELPDESK_REJECTED,PROVIDER_REJECTED]. Found [%s]", status)
                );
        }

        // Set modified on only the first time the status changes
        if (status != draft.getStatus()) {
            draft.setModifiedOn(ZonedDateTime.now());
        }
        draft.setStatus(status);
    }

    @Transactional(readOnly = false)
    default void delete(UUID publisherKey, UUID assetKey) {
        Assert.notNull(publisherKey, "Expected a non-null publisher key");
        Assert.notNull(assetKey, "Expected a non-null asset key");

        // Check draft
        final ProviderAssetDraftEntity draft = this.findOneByPublisherAndKey(publisherKey, assetKey).orElse(null);

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }

        this.delete(draft);
    }

}
