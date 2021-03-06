package eu.opertusmundi.common.repository.contract;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.ProviderTemplateContractHistoryEntity;
import eu.opertusmundi.common.domain.ProviderTemplateSectionHistoryEntity;
import eu.opertusmundi.common.model.contract.ProviderTemplateSectionDraftDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateSectionDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateSectionHistoryDto;
import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.BasicMessageCode;

@Repository
@Transactional(readOnly = true)
public interface ProviderTemplateSectionHistoryRepository extends JpaRepository<ProviderTemplateSectionHistoryEntity, Integer> {

	
	Optional<ProviderTemplateSectionHistoryEntity> findOneById(Integer id);
	
	//Optional<SectionEntity> findOneByNameAndIdNot(String name, Integer id);

	//Page<SectionEntity> findAllByNameContains(Pageable pageable, String name);

	@Query("SELECT o FROM ProviderSectionHistory o WHERE :sectionId IS NULL OR o.id = :sectionId")
	List<ProviderTemplateSectionHistoryEntity> findAll(
		@Param("sectionId") Integer sectionId,
		Sort sort
	);
	
	@Query("SELECT c FROM ProviderContractHistory c WHERE c.id = :contractId")
    Optional<ProviderTemplateContractHistoryEntity> findContract(@Param("contractId") Integer contractId);
	
	@Transactional(readOnly = false)
	default ProviderTemplateSectionHistoryDto saveFrom(ProviderTemplateSectionHistoryDto s) {

		ProviderTemplateSectionHistoryEntity sectionEntity = null;
		sectionEntity = this.findById(s.getId()).orElse(null);
		if (sectionEntity == null) {
			
			// Create a new entity
			sectionEntity = new ProviderTemplateSectionHistoryEntity();
		}
		else if(sectionEntity.getContract().getId()!=s.getContract().getId()){
			// Create a new entity if this id exists in another contract
			sectionEntity = new ProviderTemplateSectionHistoryEntity();
		}
		
		//final ContractEntity e = contractRepository.findById(s.getContract().getId()).get();
		sectionEntity.setContract(this.findContract(s.getContract().getId()).get());
		sectionEntity.setId(s.getId());
		sectionEntity.setMasterSectionId(s.getMasterSectionId());
		sectionEntity.setOptional(s.isOptional());
		sectionEntity.setOption(s.getOption());
		sectionEntity.setSuboption(s.getSuboption());
		return saveAndFlush(sectionEntity).toDto();
	}
	
	@Transactional(readOnly = false)
	default ProviderTemplateSectionHistoryDto saveFrom(ProviderTemplateSectionDto s, ProviderTemplateContractHistoryEntity ce) {

		ProviderTemplateSectionHistoryEntity sectionEntity = null;
		sectionEntity = this.findById(s.getId()).orElse(null);
		if (sectionEntity == null) {
			
			// Create a new entity
			sectionEntity = new ProviderTemplateSectionHistoryEntity();
		}
		
		sectionEntity.setContract(ce);
		sectionEntity.setId(s.getId());
		sectionEntity.setMasterSectionId(s.getMasterSectionId());
		sectionEntity.setOptional(s.isOptional());
		sectionEntity.setOption(s.getOption());
		sectionEntity.setSuboption(s.getSuboption());
		return saveAndFlush(sectionEntity).toDto();
	}
	
	@Transactional(readOnly = false)
	default ProviderTemplateSectionHistoryDto saveFrom(ProviderTemplateSectionDraftDto s, ProviderTemplateContractHistoryEntity ce) {

		
		// Create a new entity
		ProviderTemplateSectionHistoryEntity sectionEntity = new ProviderTemplateSectionHistoryEntity();
		
		
		sectionEntity.setContract(ce);
		sectionEntity.setId(s.getId());
		sectionEntity.setMasterSectionId(s.getMasterSectionId());
		sectionEntity.setOptional(s.isOptional());
		sectionEntity.setOption(s.getOption());
		sectionEntity.setSuboption(s.getSuboption());
		return saveAndFlush(sectionEntity).toDto();
	}

	@Transactional(readOnly = false)
	default void remove(int id) {

		ProviderTemplateSectionHistoryEntity sectionEntity = this.findById(id).orElse(null);

		if (sectionEntity == null) {
			throw ApplicationException.fromMessage(
				BasicMessageCode.RecordNotFound, 
				"Record not found"
			);
		}

		this.deleteById(id);
	}

}
