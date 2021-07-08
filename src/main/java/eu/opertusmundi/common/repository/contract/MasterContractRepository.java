package eu.opertusmundi.common.repository.contract;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.MasterContractDraftEntity;
import eu.opertusmundi.common.domain.MasterContractEntity;
import eu.opertusmundi.common.domain.HelpdeskAccountEntity;
import eu.opertusmundi.common.domain.MasterSectionDraftEntity;
import eu.opertusmundi.common.domain.MasterSectionEntity;
import eu.opertusmundi.common.model.contract.MasterContractDraftDto;
import eu.opertusmundi.common.model.contract.MasterContractDto;
import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.BasicMessageCode;

@Repository
@Transactional(readOnly = true)
public interface MasterContractRepository extends JpaRepository<MasterContractEntity, Integer> {

	Optional<MasterContractEntity> findOneById(Integer id);
	
	@Query("SELECT s FROM Contract s WHERE s.key = :key")
	Optional<MasterContractEntity> findByKey(UUID key);
	
	@Query("SELECT a FROM HelpdeskAccount a WHERE a.id = :id")
	    HelpdeskAccountEntity findAccountById(
			@Param("id") int id);
	
	@Query("SELECT c FROM Contract c")
	List<MasterContractEntity> findAllContracts();
	
	@Query("SELECT s FROM Section s WHERE s.contract = :contract")
	List<MasterSectionEntity> findSectionsByContract(
		@Param("contract") MasterContractEntity contract
	);
	
	@Query("SELECT s FROM SectionDraft s WHERE s.contract = :contract")
	List<MasterSectionDraftEntity> findDraftSectionsByContract(
		@Param("contract") MasterContractDraftEntity contract
	);
	
	@Query("SELECT id FROM Section s WHERE s.contract = :contract")
	List<Integer> findSectionsIdsByContract(
		@Param("contract") MasterContractEntity contract
	);
	
	
	@Query("SELECT c FROM Contract c WHERE c.account = :account")
	List<MasterContractEntity> findContractsByAccount(
		@Param("account") HelpdeskAccountEntity account);
	
	@Transactional(readOnly = false)
	default MasterContractDto saveFrom(MasterContractDto s) {
		MasterContractEntity contractEntity = null;
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
			contractEntity = new MasterContractEntity();
			contractEntity.setCreatedAt(ZonedDateTime.now());
		}
		contractEntity.setTitle(s.getTitle());
		contractEntity.setSubtitle(s.getSubtitle());
		contractEntity.setId(s.getId());
		contractEntity.setAccount(this.findAccountById(s.getAccount().getId()));
		contractEntity.setState(s.getState());
		contractEntity.setVersion(s.getVersion());
		contractEntity.setModifiedAt(ZonedDateTime.now());
		return saveAndFlush(contractEntity).toDto();
	}
	
	@Transactional(readOnly = false)
	default MasterContractDto saveFrom(MasterContractDraftDto s) {
		MasterContractEntity contractEntity = null;
			contractEntity = new MasterContractEntity();
			contractEntity.setCreatedAt(ZonedDateTime.now());
		//}
		contractEntity.setTitle(s.getTitle());
		contractEntity.setSubtitle(s.getSubtitle());
		contractEntity.setId(s.getId());
		contractEntity.setParentId(s.getParentId());
		contractEntity.setAccount(this.findAccountById(s.getAccount().getId()));
		contractEntity.setState(s.getState());
		contractEntity.setVersion(s.getVersion());
		contractEntity.setModifiedAt(ZonedDateTime.now());
		return saveAndFlush(contractEntity).toDto();
	}

	@Transactional(readOnly = false)
	default void remove(int id) {

		MasterContractEntity ContractEntity = this.findById(id).orElse(null);

		if (ContractEntity == null) {
			throw ApplicationException.fromMessage(
				BasicMessageCode.RecordNotFound, 
				"Record not found"
			);
		}

		this.deleteById(id);
	}
	

}
