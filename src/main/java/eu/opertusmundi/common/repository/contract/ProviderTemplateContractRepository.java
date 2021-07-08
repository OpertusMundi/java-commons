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

import eu.opertusmundi.common.domain.ProviderTemplateContractDraftEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractEntity;
import eu.opertusmundi.common.domain.ProviderTemplateSectionDraftEntity;
import eu.opertusmundi.common.domain.ProviderTemplateSectionEntity;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractDraftDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractDto;
import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.BasicMessageCode;

@Repository
@Transactional(readOnly = true)
public interface ProviderTemplateContractRepository extends JpaRepository<ProviderTemplateContractEntity, Integer> {

	Optional<ProviderTemplateContractEntity> findOneById(Integer id);

	@Query("SELECT c FROM ProviderContract c WHERE c.key = :key")
	Optional<ProviderTemplateContractEntity> findByKey(UUID key);

	
	
	@Query("SELECT s FROM ProviderSection s WHERE s.contract = :contract")
	List<ProviderTemplateSectionEntity> findSectionsByContract(
		@Param("contract") ProviderTemplateContractEntity contract
	);
	
	@Query("SELECT s FROM ProviderSectionDraft s WHERE s.contract = :contract")
	List<ProviderTemplateSectionDraftEntity> findDraftSectionsByContract(
		@Param("contract") ProviderTemplateContractDraftEntity contract
	);
	
	@Query("SELECT id FROM ProviderSection s WHERE s.contract = :contract")
	List<Integer> findSectionsIdsByContract(
		@Param("contract") ProviderTemplateContractEntity contract
	);
	
	
	@Query("SELECT c FROM ProviderContract c WHERE c.providerKey = :providerKey")
	List<ProviderTemplateContractEntity> findContractsByProviderKey(
		@Param("providerKey") UUID providerKey);
	
	@Transactional(readOnly = false)
	default ProviderTemplateContractDto saveFrom(ProviderTemplateContractDto s) {
		ProviderTemplateContractEntity contractEntity = null;
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
			contractEntity = new ProviderTemplateContractEntity();
			contractEntity.setCreatedAt(ZonedDateTime.now());
		}
		contractEntity.setTitle(s.getTitle());
		contractEntity.setSubtitle(s.getSubtitle());
		contractEntity.setId(s.getId());
		contractEntity.setMasterContractId(s.getMasterContractId());
		contractEntity.setMasterContractVersion(s.getMasterContractVersion());
		contractEntity.setProviderKey(s.getProviderKey());
		contractEntity.setParentId(s.getParentId());
		contractEntity.setVersion(s.getVersion());
		contractEntity.setModifiedAt(ZonedDateTime.now());
		return saveAndFlush(contractEntity).toDto();
	}
	
	@Transactional(readOnly = false)
	default ProviderTemplateContractDto saveFrom(ProviderTemplateContractDraftDto s) {
		ProviderTemplateContractEntity contractEntity = null;
			contractEntity = new ProviderTemplateContractEntity();
			contractEntity.setCreatedAt(ZonedDateTime.now());
		//}
		contractEntity.setTitle(s.getTitle());
		contractEntity.setSubtitle(s.getSubtitle());
		contractEntity.setId(s.getId());
		contractEntity.setMasterContractId(s.getMasterContractId());
		contractEntity.setMasterContractVersion(s.getMasterContractVersion());
		contractEntity.setProviderKey(s.getProviderKey());
		contractEntity.setParentId(s.getParentId());
		contractEntity.setVersion(s.getVersion());
		contractEntity.setModifiedAt(ZonedDateTime.now());
		return saveAndFlush(contractEntity).toDto();
	}

	@Transactional(readOnly = false)
	default void remove(int id) {

		ProviderTemplateContractEntity ContractEntity = this.findById(id).orElse(null);

		if (ContractEntity == null) {
			throw ApplicationException.fromMessage(
				BasicMessageCode.RecordNotFound, 
				"Record not found"
			);
		}

		this.deleteById(id);
	}


}
