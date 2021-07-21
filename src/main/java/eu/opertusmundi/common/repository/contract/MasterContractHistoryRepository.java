package eu.opertusmundi.common.repository.contract;

import java.time.ZonedDateTime;
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

import eu.opertusmundi.common.domain.MasterContractDraftEntity;
import eu.opertusmundi.common.domain.MasterContractEntity;
import eu.opertusmundi.common.domain.MasterContractHistoryEntity;
import eu.opertusmundi.common.domain.MasterContractHistoryViewEntity;
import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.contract.ContractMessageCode;
import eu.opertusmundi.common.model.contract.EnumContractStatus;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractDto;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractHistoryDto;

@Repository
@Transactional(readOnly = true)
public interface MasterContractHistoryRepository extends JpaRepository<MasterContractHistoryEntity, Integer> {

    @Query("SELECT c FROM ContractHistory c WHERE c.key = :key")
    Optional<MasterContractHistoryEntity> findByKey(UUID key);

    @Query("SELECT c FROM ContractDraft c WHERE c.id = :id")
    Optional<MasterContractDraftEntity> findDraftById(int id);

    @Query("SELECT c FROM ContractHistory c WHERE c.status = 'ACTIVE' and c.published.id = :id")
    Optional<MasterContractHistoryEntity> findHistoryByPublishedContractId(int id);

    @Modifying
    @Query("DELETE ContractDraft c WHERE c.id = :id")
    int deleteDraftById(int id);

    @Query("SELECT c FROM ContractHistoryView c WHERE "
         + "(c.status in ('ACTIVE', 'INACTIVE', 'DRAFT')) and "
         + "(c.status in :status or :status is null) and "
         + "(c.title like :title or :title is null) "
    )
    Page<MasterContractHistoryViewEntity> findHistory(String title, Set<EnumContractStatus> status, Pageable pageable);

    default Page<MasterContractHistoryDto> findHistoryObjects(
        String title, Set<EnumContractStatus> status, Pageable pageable
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
        return this.findHistory(title, status, pageable).map(c -> c.toDto(false));
    }

    @Transactional(readOnly = false)
    default MasterContractHistoryDto deactivate(int id) throws ApplicationException {
        // Get published contract
        final MasterContractHistoryEntity history = this.findById(id).orElse(null);

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

        history.setStatus(EnumContractStatus.INACTIVE);
        history.setModifiedAt(ZonedDateTime.now());

        if (history.getPublished() != null) {
            history.getPublished().setParent(null);
            history.setPublished(null);
        }

        return this.saveAndFlush(history).toDto(true);
    }

    @Transactional(readOnly = false)
    default MasterContractDto publishDraft(int id) throws ApplicationException {
        // Get draft
        final MasterContractDraftEntity draft = this.findDraftById(id).orElse(null);

        if (draft == null) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.DRAFT_NOT_FOUND, "Record not found"
            );
        }

        // Create history record
        final MasterContractHistoryEntity history = MasterContractHistoryEntity.from(draft);
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
        this.deleteDraftById(id);

        // Delete parent published contract
        if (history.getContractParent().getPublished() != null) {
            history.getContractParent().getPublished().setParent(null);
            history.getContractParent().setPublished(null);
        }

        // Create published contract
        final MasterContractEntity published = MasterContractEntity.from(history);
        history.setPublished(published);

        // Refresh data store and return the new published contract
        this.saveAndFlush(history);

        return history.getPublished().toDto(true);
    }

}
