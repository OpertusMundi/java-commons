package eu.opertusmundi.common.repository.contract;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.MasterContractDraftEntity;
import eu.opertusmundi.common.domain.MasterContractEntity;
import eu.opertusmundi.common.domain.MasterContractHistoryEntity;
import eu.opertusmundi.common.domain.HelpdeskAccountEntity;
import eu.opertusmundi.common.domain.MasterSectionHistoryEntity;
import eu.opertusmundi.common.model.contract.MasterContractHistoryDto;
import eu.opertusmundi.common.domain.MasterContractEntity;
import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.BasicMessageCode;

@Repository
@Transactional(readOnly = true)
public interface MasterContractHistoryRepository extends JpaRepository<MasterContractHistoryEntity, Integer> {

	Optional<MasterContractHistoryEntity> findOneById(Integer id);
	
	//Optional<ContractEntity> findOneByNameAndIdNot(String name, Integer id);

	//Page<ContractEntity> findAllByNameContains(Pageable pageable, String name);

	//@Query("SELECT c FROM Section c WHERE :contractId IS NULL OR c.id = :contractId")
	//List<ContractEntity> findAll(
	//	@Param("id") Integer sectionId,
	//	Sort sort
	//);
	
	@Query("SELECT a FROM HelpdeskAccount a WHERE a.id = :id")
	    HelpdeskAccountEntity findAccountById(
			@Param("id") int id);
	
	@Query("SELECT c FROM ContractHistory c WHERE c.parentId = :parentId AND c.version = :version ")
    Optional<MasterContractHistoryEntity> findMasterTemplateContractHistoryEntity(
    		Integer parentId, @Param("version") String version);
	
	@Query("SELECT o FROM SectionHistory o WHERE o.contract = :contract")
	List<MasterSectionHistoryEntity> findSectionsByContract(
		@Param("contract") MasterContractHistoryEntity contract
	);
	
	@Query("SELECT c FROM ContractHistory c WHERE c.account = :account")
	List<MasterContractEntity> findHistoryContractsByAccount(
		@Param("account") HelpdeskAccountEntity account);
	
	@Query("SELECT c FROM ContractHistory c WHERE c.parentId = :parentId")
	List<MasterContractHistoryEntity> findContractVersions(
			Integer parentId);

	
	@Query("SELECT c FROM ContractHistory c WHERE c.id = :id and c.version = :version")
	List<MasterContractEntity> findContractByIdVersion(
		@Param("id") Integer id, @Param("version") String version);
	
	@Transactional(readOnly = false)
	default MasterContractHistoryDto saveFrom(MasterContractEntity s) {
		MasterContractHistoryEntity MasterTemplateContractHistoryEntity = null;
		MasterTemplateContractHistoryEntity = this.findMasterTemplateContractHistoryEntity(s.getParentId(), s.getVersion()).orElse(null);
		if (MasterTemplateContractHistoryEntity == null) {
			// Create a new entity
			MasterTemplateContractHistoryEntity = new MasterContractHistoryEntity();
			MasterTemplateContractHistoryEntity.setCreatedAt(ZonedDateTime.now());

		}
		MasterTemplateContractHistoryEntity.setTitle(s.getTitle());
		MasterTemplateContractHistoryEntity.setSubtitle(s.getSubtitle());
		MasterTemplateContractHistoryEntity.setParentId(s.getParentId());
		MasterTemplateContractHistoryEntity.setAccount(this.findAccountById(s.getAccount().getId()));
		MasterTemplateContractHistoryEntity.setState(s.getState());
		MasterTemplateContractHistoryEntity.setVersion(s.getVersion());
		MasterTemplateContractHistoryEntity.setModifiedAt(ZonedDateTime.now());
		return saveAndFlush(MasterTemplateContractHistoryEntity).toDto();
		
	}
	
	@Transactional(readOnly = false)
	default MasterContractHistoryDto saveFrom(MasterContractDraftEntity s) {
		MasterContractHistoryEntity MasterTemplateContractHistoryEntity = null;
		MasterTemplateContractHistoryEntity = this.findMasterTemplateContractHistoryEntity(s.getParentId(), s.getVersion()).orElse(null);
		if (MasterTemplateContractHistoryEntity == null) {
			// Create a new entity
			MasterTemplateContractHistoryEntity = new MasterContractHistoryEntity();
			MasterTemplateContractHistoryEntity.setCreatedAt(ZonedDateTime.now());

		}
		MasterTemplateContractHistoryEntity.setTitle(s.getTitle());
		MasterTemplateContractHistoryEntity.setSubtitle(s.getSubtitle());
		MasterTemplateContractHistoryEntity.setParentId(s.getParentId());
		MasterTemplateContractHistoryEntity.setAccount(this.findAccountById(s.getAccount().getId()));
		MasterTemplateContractHistoryEntity.setState(s.getState());
		MasterTemplateContractHistoryEntity.setVersion(s.getVersion());
		MasterTemplateContractHistoryEntity.setModifiedAt(ZonedDateTime.now());
		return saveAndFlush(MasterTemplateContractHistoryEntity).toDto();
		
	}

	@Transactional(readOnly = false)
	default void remove(int id) {

		MasterContractHistoryEntity MasterTemplateContractHistoryEntity = this.findById(id).orElse(null);

		if (MasterTemplateContractHistoryEntity == null) {
			throw ApplicationException.fromMessage(
				BasicMessageCode.RecordNotFound, 
				"Record not found"
			);
		}

		this.deleteById(id);
	}

}
