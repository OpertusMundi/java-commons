package eu.opertusmundi.common.repository.contract;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.MasterContractHistoryEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractDraftEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractHistoryEntity;
import eu.opertusmundi.common.domain.ProviderTemplateSectionDraftEntity;
import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.contract.ContractMessageCode;
import eu.opertusmundi.common.model.contract.EnumContractStatus;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractCommandDto;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractDto;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateSectionDto;

@Repository
@Transactional(readOnly = true)
public interface ProviderTemplateContractDraftRepository extends JpaRepository<ProviderTemplateContractDraftEntity, Integer> {

    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<AccountEntity> findAccountById(int id);

    @Query("SELECT a FROM Account a WHERE a.key = :key")
    Optional<AccountEntity> findAccountByKey(UUID key);

    @Query("SELECT c FROM ContractHistory c WHERE c.status = 'ACTIVE' and c.defaultContract = true and c.key = :key")
    Optional<MasterContractHistoryEntity> findDefaultMasterContract(UUID key);

    @Query("SELECT c FROM ContractHistory c WHERE c.status = 'ACTIVE' and c.defaultContract = true")
    List<MasterContractHistoryEntity> findDefaultMasterContracts();

    @Query("SELECT c FROM ContractHistory c WHERE c.key = :contractKey and status = 'ACTIVE'")
    Optional<MasterContractHistoryEntity> findActiveMasterContractByKey(UUID contractKey);

    @Query("SELECT c FROM ProviderContractHistory c WHERE c.owner.key = :providerKey and c.key = :contractKey")
    Optional<ProviderTemplateContractHistoryEntity> findProviderTemplateContractHistoryByKey(UUID providerKey, UUID contractKey);

    @Query("SELECT c FROM ProviderContractDraft c WHERE c.owner.key = :providerKey and c.key = :draftKey")
    Optional<ProviderTemplateContractDraftEntity> findOneByKey(UUID providerKey, UUID draftKey);

    @Query("SELECT c FROM ProviderContractDraft c WHERE c.owner.id = :providerId and c.key = :draftKey")
    Optional<ProviderTemplateContractDraftEntity> findOneByKey(Integer providerId, UUID draftKey);

    @Query("SELECT  c FROM ProviderContractDraft c LEFT OUTER JOIN c.parent p LEFT OUTER JOIN c.template t WHERE c.owner.key = :providerKey")
    Page<ProviderTemplateContractDraftEntity> findAll(UUID providerKey, Pageable pageable);

    default Page<ProviderTemplateContractDto> findAllObjects(UUID providerKey, Pageable pageable) {
        return this.findAll(providerKey, pageable).map(c -> c.toDto(false));
    }

    default Optional<ProviderTemplateContractDto> findOneObject(UUID providerKey, UUID draftKey) {
        return this.findOneByKey(providerKey, draftKey).map(c -> c.toDto(true));
    }

    @Transactional(readOnly = false)
    default ProviderTemplateContractDto deleteByKey(Integer providerId, UUID draftKey) throws ApplicationException {
        final ProviderTemplateContractDraftEntity e = this.findOneByKey(providerId, draftKey).orElse(null);

        if (e == null) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.DRAFT_NOT_FOUND, "Record not found"
            );
        }

        final ProviderTemplateContractDto result = e.toDto(true);

        // Remove parent link
        if (e.getParent() != null) {
            e.getParent().setDraft(null);
            e.setParent(null);
        }

        this.delete(e);

        return result;
    }

    @Transactional(readOnly = false)
    default ProviderTemplateContractDto createFromHistory(UUID providerKey, UUID templateKey) throws ApplicationException {
        final ProviderTemplateContractHistoryEntity parent = this.findProviderTemplateContractHistoryByKey(
            providerKey, templateKey
        ).orElse(null);

        if (parent == null) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.HISTORY_NOT_FOUND, "Record not found"
            );
        }

        if(parent.isDefaultContract()) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.HISTORY_CONTRACT_IS_DEFAULT, "Cannot create new version of default contract"
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

        final ProviderTemplateContractDraftEntity e = ProviderTemplateContractDraftEntity.from(parent);

        final AccountEntity owner = this.findAccountByKey(providerKey).orElse(null);
        if (owner == null) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.ACCOUNT_NOT_FOUND, "Record not found"
            );
        }
        e.setOwner(owner);

        return this.saveAndFlush(e).toDto(true);
    }

	@Transactional(readOnly = false)
	default ProviderTemplateContractDto saveFrom(ProviderTemplateContractCommandDto c) throws ApplicationException {
        ProviderTemplateContractDraftEntity e = null;

        if (c.getDraftKey() != null) {
            e = this.findOneByKey(c.getUserId(), c.getDraftKey()).orElse(null);
            if (e == null) {
                throw ApplicationException.fromMessage(
                    ContractMessageCode.DRAFT_NOT_FOUND, "Record not found"
                );
            }
            e.setModifiedAt(ZonedDateTime.now());
        } else {
            e = new ProviderTemplateContractDraftEntity();
            e.setCreatedAt(ZonedDateTime.now());
            e.setModifiedAt(e.getCreatedAt());
            e.setVersion("1");

            final AccountEntity owner = this.findAccountById(c.getUserId()).orElse(null);
            if (owner == null) {
                throw ApplicationException.fromMessage(
                    ContractMessageCode.ACCOUNT_NOT_FOUND, "Record not found"
                );
            }
            e.setOwner(owner);

            final MasterContractHistoryEntity template = this.findActiveMasterContractByKey(c.getTemplateKey()).orElse(null);
            if (template == null) {
                throw ApplicationException.fromMessage(
                    ContractMessageCode.MASTER_CONTRACT_NOT_FOUND, "Record not found"
                );
            }
            e.setTemplate(template);
        }

        e.setSubtitle(c.getSubtitle());
        e.setTitle(c.getTitle());

        // Add sections
        e.getSections().clear();

        for (final ProviderTemplateSectionDto s : c.getSections()) {
            final ProviderTemplateSectionDraftEntity section = ProviderTemplateSectionDraftEntity.from(s);

            section.setContract(e);
            section.setMasterSection(e.getTemplate().findSectionById(s.getMasterSectionId()));
            if (section.getMasterSection() == null) {
                throw ApplicationException.fromMessage(
                    ContractMessageCode.MASTER_SECTION_NOT_FOUND, "Record not found"
                );
            }

            e.getSections().add(section);
        }

		return saveAndFlush(e).toDto(true);
	}

    @Transactional(readOnly = false)
    default void remove(UUID providerKey, UUID draftKey) throws ApplicationException {
        final ProviderTemplateContractDraftEntity e = this.findOneByKey(providerKey, draftKey).orElse(null);

        if (e == null) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.DRAFT_NOT_FOUND, "Record not found"
            );
        }

        this.deleteById(e.getId());
    }

    @Transactional(readOnly = false)
    default ProviderTemplateContractDto createDefaultContractDraft(UUID providerKey, UUID contractKey) throws ApplicationException {
        final var masterDefaultContract = this.findDefaultMasterContract(contractKey).orElse(null);
        if (masterDefaultContract == null) {
            throw ApplicationException.fromMessage(
                ContractMessageCode.DEFAULT_MASTER_CONTRACT_NOT_FOUND,
                "Default master contract was not found"
            );
        }
        final AccountEntity owner = this.findAccountByKey(providerKey).orElse(null);
        if (owner == null) {
            throw ApplicationException.fromMessage(ContractMessageCode.ACCOUNT_NOT_FOUND, "Record not found");
        }

        final var draft = new ProviderTemplateContractDraftEntity();
        draft.setCreatedAt(ZonedDateTime.now());
        draft.setDefaultContract(true);
        draft.setModifiedAt(draft.getCreatedAt());
        draft.setVersion(masterDefaultContract.getVersion());
        draft.setOwner(owner);
        draft.setParent(null);
        draft.setSubtitle(masterDefaultContract.getSubtitle());
        draft.setTemplate(masterDefaultContract);
        draft.setTitle(masterDefaultContract.getTitle());

        // Add sections
        draft.getSections().clear();
        for (final var mSection : masterDefaultContract.getSections()) {
            final var pSection = new ProviderTemplateSectionDraftEntity();

            pSection.setContract(draft);
            pSection.setMasterSection(mSection);
            if (mSection.getOptions().size() > 0) {
                pSection.setOption(0);
                pSection.setOptional(false);
                pSection.setSubOption(IntStream.range(0, mSection.getOptions().size()).boxed().collect(Collectors.toList()));
            }

            draft.getSections().add(pSection);
        }

        return saveAndFlush(draft).toDto(true);
    }
}
