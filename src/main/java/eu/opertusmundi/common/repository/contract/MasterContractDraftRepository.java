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
import eu.opertusmundi.common.domain.HelpdeskAccountEntity;
import eu.opertusmundi.common.domain.MasterSectionDraftEntity;
import eu.opertusmundi.common.model.contract.MasterContractDraftDto;
import eu.opertusmundi.common.model.contract.MasterContractDto;
import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.BasicMessageCode;

@Repository
@Transactional(readOnly = true)
public interface MasterContractDraftRepository extends JpaRepository<MasterContractDraftEntity, Integer> {

	Optional<MasterContractEntity> findOneById(Integer id);
	
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
	
	@Query("SELECT s FROM SectionDraft s WHERE s.contract = :contract")
	List<MasterSectionDraftEntity> findSectionsByContract(
		@Param("contract") MasterContractDraftEntity contract
	);
	
	@Query("SELECT id FROM SectionDraft s WHERE s.contract = :contract")
	List<Integer> findSectionsIdsByContract(
		@Param("contract") MasterContractDraftEntity contract
	);
	
	
	@Query("SELECT c FROM ContractDraft c WHERE c.account = :account")
	List<MasterContractDraftEntity> findContractsByAccount(
		@Param("account") HelpdeskAccountEntity account);
	
	@Transactional(readOnly = false)
	default MasterContractDraftDto saveFrom(MasterContractDraftDto s) {
		MasterContractDraftEntity contractEntity = null;
		if (s.getId() != null) {
			// Retrieve entity from repository
			contractEntity = this.findById(s.getId()).orElse(null);
			if (contractEntity == null) {
				throw ApplicationException.fromMessage(
					BasicMessageCode.RecordNotFound, 
					"Record not found"
				);
			}
		} else {
			// Create a new entity
			contractEntity = new MasterContractDraftEntity();
			contractEntity.setCreatedAt(ZonedDateTime.now());
		}
		contractEntity.setTitle(s.getTitle());
		contractEntity.setSubtitle(s.getSubtitle());
		contractEntity.setId(s.getId());
		contractEntity.setParentId(s.getId());
		contractEntity.setAccount(this.findAccountById(s.getAccount().getId()));
		contractEntity.setState(s.getState());
		contractEntity.setVersion(s.getVersion());
		contractEntity.setModifiedAt(ZonedDateTime.now());
		return saveAndFlush(contractEntity).toDto();
	}
	
	@Transactional(readOnly = false)
	default MasterContractDraftDto saveFrom(MasterContractDto s) {
		MasterContractDraftEntity contractEntity = null;
		
		contractEntity = new MasterContractDraftEntity();
		contractEntity.setCreatedAt(s.getCreatedAt());
		contractEntity.setTitle(s.getTitle());
		contractEntity.setSubtitle(s.getSubtitle());
		contractEntity.setId(s.getId());
		contractEntity.setParentId(s.getId());
		contractEntity.setAccount(this.findAccountById(s.getAccount().getId()));
		contractEntity.setState("DRAFT");
		contractEntity.setVersion(s.getVersion());
		contractEntity.setModifiedAt(ZonedDateTime.now());
		return saveAndFlush(contractEntity).toDto();
	}
	

	@Transactional(readOnly = false)
	default void remove(int id) {

		MasterContractDraftEntity ContractEntity = this.findById(id).orElse(null);

		if (ContractEntity == null) {
			throw ApplicationException.fromMessage(
				BasicMessageCode.RecordNotFound, 
				"Record not found"
			);
		}

		this.deleteById(id);
	}

}
