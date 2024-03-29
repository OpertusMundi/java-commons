package eu.opertusmundi.common.repository.contract;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.MasterContractHistoryEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractDraftEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractHistoryEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractHistoryViewEntity;
import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.contract.ContractDto;
import eu.opertusmundi.common.model.contract.ContractMessageCode;
import eu.opertusmundi.common.model.contract.EnumContractStatus;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractDto;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractHistoryDto;

@Repository
@Transactional(readOnly = true)
public interface ProviderTemplateContractHistoryRepository extends JpaRepository<ProviderTemplateContractHistoryEntity, Integer> {

    @Query("SELECT c FROM ProviderContractHistory c "
         + "WHERE c.key = :contractKey and c.owner.key = :providerKey and c.status = 'ACTIVE' and c.defaultContract = true")
    Optional<ProviderTemplateContractHistoryEntity> findDefaultProviderContract(UUID providerKey, UUID contractKey);

    @Query("SELECT c FROM ProviderContractHistory c WHERE c.owner.key = :providerKey and c.status = 'ACTIVE' and c.defaultContract = true")
    List<ProviderTemplateContractHistoryEntity> findDefaultProviderContracts(UUID providerKey);

    @Query("SELECT c FROM ProviderContractHistory c WHERE c.owner.id = :providerId and c.key = :contractKey")
    Optional<ProviderTemplateContractHistoryEntity> findByKey(Integer providerId, UUID contractKey);

    @Query("SELECT c FROM ProviderContractHistory c WHERE c.owner.key = :providerKey and c.key = :contractKey")
    Optional<ProviderTemplateContractHistoryEntity> findByKey(UUID providerKey, UUID contractKey);

    @Query("SELECT c FROM ProviderContractHistory c WHERE "
         + "c.owner.key = :providerKey and "
         + "c.id = :contractId and "
         + "c.version = :contractVersion "
    )
    Optional<ProviderTemplateContractHistoryEntity> findByIdAndVersion(UUID providerKey, Integer contractId, String contractVersion);

    @Query("SELECT c FROM ProviderContractDraft c WHERE c.owner.key = :providerKey and c.key = :draftKey")
    Optional<ProviderTemplateContractDraftEntity> findDraftByKey(UUID providerKey, UUID draftKey);

    @Query("SELECT c FROM ProviderContractDraft c WHERE c.owner.id = :providerId and c.key = :draftKey")
    Optional<ProviderTemplateContractDraftEntity> findDraftByKey(Integer providerId, UUID draftKey);

    @Query("SELECT c FROM ContractHistory c WHERE c.key = :contractKey and status = 'ACTIVE'")
    Optional<MasterContractHistoryEntity> findActiveMasterContractByKey(UUID contractKey);

    @Query("SELECT c FROM ProviderContractHistory c WHERE "
         + "c.status = 'ACTIVE' and "
         + "c.owner.key = :providerKey and "
         + "c.published.key = :contractKey"
    )
    Optional<ProviderTemplateContractHistoryEntity> findHistoryByPublishedContractKey(UUID providerKey, UUID contractKey);

    @Modifying
    @Query("DELETE ProviderContractDraft c WHERE c.id = :id")
    int deleteDraftById(Integer id);

    @Query("SELECT c FROM ProviderContractHistoryView c WHERE "
         + "(c.owner.key = :providerKey) and "
         + "(c.status in ('ACTIVE', 'INACTIVE', 'DRAFT')) and "
         + "(c.status in :status or :status is null) and "
         + "(c.title like :title or :title is null) "
    )
    Page<ProviderTemplateContractHistoryViewEntity> findHistory(
        UUID providerKey, String title, Set<EnumContractStatus> status, Pageable pageable
    );

    default ProviderTemplateContractHistoryDto findOneObjectByKey(UUID providerKey, UUID contractKey) {
        return this.findByKey(providerKey, contractKey).map(ProviderTemplateContractHistoryEntity::toDto).orElse(null);
    }

    default ContractDto findOneObjectByIdAndVersion(UUID providerKey, Integer contractId, String version) {
        return this.findByIdAndVersion(providerKey, contractId, version)
            .map(ProviderTemplateContractHistoryEntity::toSimpleDto)
            .orElse(null);
    }

    default Page<ProviderTemplateContractHistoryDto> findHistoryObjects(
        UUID providerKey, String title, Set<EnumContractStatus> status, Pageable pageable
    ) {
        if (StringUtils.isBlank(title)) {
            title = null;
        } else {
            if (!title.startsWith("%")) {
                title = "%" + title;
            }
            if (!title.endsWith("%")) {
                title = title + "%";
            }
        }
        if (status != null && status.isEmpty()) {
            status = null;
        }
        return this.findHistory(providerKey, title, status, pageable).map(c -> c.toDto(false));
    }

    @Transactional(readOnly = false)
    default ProviderTemplateContractHistoryDto deactivate(UUID providerKey, UUID contractKey, boolean force) throws ApplicationException {
        // Get published contract
        final ProviderTemplateContractHistoryEntity history = this.findByKey(providerKey, contractKey).orElse(null);

        if (history == null) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.HISTORY_NOT_FOUND, "Record not found"
            );
        }

        if (history.getStatus() != EnumContractStatus.ACTIVE) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.INVALID_STATUS,
                String.format("Invalid status [%s] found. Expected status to be [ACTIVE]", history.getStatus())
            );
        }

        if (history.isDefaultContract() && !force) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.DEFAULT_PROVIDER_CONTRACT_DEACTIVATE,
                String.format("Cannot deactivate the default contract", history.getStatus())
            );
        }

        history.setStatus(EnumContractStatus.INACTIVE);
        history.setModifiedAt(ZonedDateTime.now());

        if (history.getPublished() != null) {
            history.getPublished().setParent(null);
            history.setPublished(null);
        }

        return this.saveAndFlush(history).toDto(true);
    }

    @Transactional(readOnly = false)
    default ProviderTemplateContractDto publishDraft(UUID providerKey, UUID draftKey) throws ApplicationException {
        // Get draft
        final ProviderTemplateContractDraftEntity draft = this.findDraftByKey(providerKey, draftKey).orElse(null);

        if (draft == null) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.DRAFT_NOT_FOUND, "Record not found"
            );
        }

        // Create history record
        final ProviderTemplateContractHistoryEntity history = ProviderTemplateContractHistoryEntity.from(draft);
        history.setStatus(EnumContractStatus.ACTIVE);
        this.saveAndFlush(history);


        // Archive parent contract
        if (history.getContractParent() != null && history.getContractParent() != history) {
            history.getContractParent().setStatus(EnumContractStatus.HISTORY);
        }

        // Set parent links
        if (history.getContractParent() == null) {
            history.setContractParent(history);
            history.setContractRoot(history);
            this.saveAndFlush(history);
        }

        // Delete draft
        this.deleteDraftById(draft.getId());

        // Delete parent published contract
        if (history.getContractParent().getPublished() != null) {
            history.getContractParent().getPublished().setParent(null);
            history.getContractParent().setPublished(null);
        }

        // Create published contract
        final ProviderTemplateContractEntity published = ProviderTemplateContractEntity.from(history);
        history.setPublished(published);

        // Refresh data store and return the new published contract
        this.saveAndFlush(history);

        return history.getPublished().toDto(true);
    }

    default ProviderTemplateContractDto acceptDefaultContract(UUID providerKey, UUID contractKey) {
        final var defaultContract = this.findDefaultProviderContract(providerKey, contractKey).orElse(null);

        if (defaultContract == null) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.DEFAULT_PROVIDER_CONTRACT_NOT_FOUND, "Contract was not found"
            );
        }

        if (defaultContract.getStatus() != EnumContractStatus.ACTIVE) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.INVALID_STATUS,
                String.format("Invalid status [%s] found. Expected status to be [ACTIVE]", defaultContract.getStatus())
            );
        }
        if (!defaultContract.isDefaultContract()) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.PROVIDER_CONTRACT_IS_NOT_DEFAULT,
                "Only a default contract can be accepted"
            );
        }

        if (!defaultContract.isDefaultContractAccepted()) {
            final var now = ZonedDateTime.now();
            defaultContract.setDefaultContractAccepted(true);
            defaultContract.setDefaultContractAcceptedAt(now);

            defaultContract.getPublished().setDefaultContractAccepted(true);
            defaultContract.getPublished().setDefaultContractAcceptedAt(now);

            this.saveAndFlush(defaultContract);
        }

        return defaultContract.toDto(true);
    }

}
