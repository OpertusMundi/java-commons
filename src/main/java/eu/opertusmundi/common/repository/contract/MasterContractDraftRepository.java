package eu.opertusmundi.common.repository.contract;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.HelpdeskAccountEntity;
import eu.opertusmundi.common.domain.MasterContractDraftEntity;
import eu.opertusmundi.common.domain.MasterContractHistoryEntity;
import eu.opertusmundi.common.domain.MasterSectionDraftEntity;
import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.contract.ContractMessageCode;
import eu.opertusmundi.common.model.contract.EnumContractStatus;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractCommandDto;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractDto;
import eu.opertusmundi.common.model.contract.helpdesk.MasterSectionDto;

@Repository
@Transactional(readOnly = true)
public interface MasterContractDraftRepository extends JpaRepository<MasterContractDraftEntity, Integer> {

    @Query("SELECT a FROM HelpdeskAccount a WHERE a.id = :id")
    Optional<HelpdeskAccountEntity> findAccountById(
        @Param("id") int id
    );

    @Query("SELECT c FROM ContractHistory c WHERE c.id = :id")
    Optional<MasterContractHistoryEntity> findMasterContractHistoryById(Integer id);

    @Query("SELECT c FROM ContractDraft c WHERE c.key = :key")
    Optional<MasterContractDraftEntity> findOneByKey(@Param("key") UUID key);

    @Query("SELECT c FROM ContractDraft c WHERE c.id = :id")
    Optional<MasterContractDraftEntity> findOneById(@Param("id") Integer id);

    @Override
    @Query("SELECT  c FROM ContractDraft c LEFT OUTER JOIN c.parent p")
    Page<MasterContractDraftEntity> findAll(Pageable pageable);

    default Page<MasterContractDto> findAllObjects(Pageable pageable) {
        return this.findAll(pageable).map(c -> c.toDto(false));
    }

    default Optional<MasterContractDto> findOneObject(int id) {
        return this.findOneById(id).map(c -> c.toDto(true));
    }

    @Transactional(readOnly = false)
    default MasterContractDto deleteById(int id) throws ApplicationException {
        final MasterContractDraftEntity e = this.findOneById(id).orElse(null);

        if (e == null) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.DRAFT_NOT_FOUND, "Record not found"
            );
        }

        final MasterContractDto result = e.toDto(true);

        // Remove parent link
        if (e.getParent() != null) {
            e.getParent().setDraft(null);
            e.setParent(null);
        }

        this.delete(e);

        return result;
    }

    @Transactional(readOnly = false)
    default MasterContractDto createFromHistory(int ownerId, int parentId) throws ApplicationException {
        final MasterContractHistoryEntity parent = this.findMasterContractHistoryById(parentId).orElse(null);

        if (parent == null) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.HISTORY_NOT_FOUND, "Record not found"
            );
        }

        if (parent.getStatus() == EnumContractStatus.HISTORY) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.INVALID_STATUS,
                "Found status [HISTORY]. Expected status in [ACTIVE, INACTIVE]"
            );
        }

        if (parent.getDraft() != null) {
            return parent.getDraft().toDto(true);
        }

        final MasterContractDraftEntity e = MasterContractDraftEntity.from(parent);

        final HelpdeskAccountEntity owner = this.findAccountById(ownerId).orElse(null);
        if (owner == null) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.ACCOUNT_NOT_FOUND, "Record not found"
            );
        }
        e.setOwner(owner);

        return this.saveAndFlush(e).toDto(true);
    }

	@Transactional(readOnly = false)
	default MasterContractDto saveFrom(MasterContractCommandDto c) throws ApplicationException {
        MasterContractDraftEntity e = null;

        if (c.getId() != null) {
            e = this.findById(c.getId()).orElse(null);
            if (e == null) {
                throw ApplicationException.fromMessage(
                    ContractMessageCode.DRAFT_NOT_FOUND, "Record not found"
                );
            }
            e.setModifiedAt(ZonedDateTime.now());
        } else {
            e = new MasterContractDraftEntity();
            e.setCreatedAt(ZonedDateTime.now());
            e.setModifiedAt(e.getCreatedAt());
            e.setVersion("1");

            final HelpdeskAccountEntity owner = this.findAccountById(c.getUserId()).orElse(null);
            if (owner == null) {
                throw ApplicationException.fromMessage(
                    ContractMessageCode.ACCOUNT_NOT_FOUND, "Record not found"
                );
            }
            e.setOwner(owner);
        }

        e.setSubtitle(c.getSubtitle());
        e.setTitle(c.getTitle());

        // Add sections
        e.getSections().clear();

        for (final MasterSectionDto s : c.getSections()) {
            final MasterSectionDraftEntity section = MasterSectionDraftEntity.from(s);

            section.setContract(e);

            e.getSections().add(section);
        }

		return saveAndFlush(e).toDto(true);
	}

    @Transactional(readOnly = false)
    default void remove(int id) throws ApplicationException {
        final MasterContractDraftEntity e = this.findById(id).orElse(null);

        if (e == null) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.DRAFT_NOT_FOUND, "Record not found"
            );
        }

        this.deleteById(id);
    }

}
