package eu.opertusmundi.common.repository.contract;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.MasterContractDraftEntity;
import eu.opertusmundi.common.domain.MasterSectionDraftEntity;
import eu.opertusmundi.common.model.contract.MasterSectionDraftDto;
import eu.opertusmundi.common.model.contract.MasterSectionDto;
import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.BasicMessageCode;

@Repository
@Transactional(readOnly = true)
public interface MasterSectionDraftRepository extends JpaRepository<MasterSectionDraftEntity, Integer> {

	
	Optional<MasterSectionDraftEntity> findOneById(Integer id);
	
	//Optional<SectionEntity> findOneByNameAndIdNot(String name, Integer id);

	//Page<SectionEntity> findAllByNameContains(Pageable pageable, String name);

	@Query("SELECT o FROM SectionDraft o WHERE :sectionId IS NULL OR o.id = :sectionId")
	List<MasterSectionDraftEntity> findAll(
		@Param("sectionId") Integer sectionId,
		Sort sort
	);
	
	
	@Query("SELECT c FROM ContractDraft c WHERE c.id = :contractId")
    Optional<MasterContractDraftEntity> findContract(@Param("contractId") Integer contractId);
	
	@Transactional(readOnly = false)
	default MasterSectionDraftDto saveFrom(MasterSectionDraftDto s) {

		MasterSectionDraftEntity sectionEntity = null;
		sectionEntity = this.findById(s.getId()).orElse(null);
		if (sectionEntity == null) {
			
			// Create a new entity
			sectionEntity = new MasterSectionDraftEntity();
		}
		else if(sectionEntity.getContract().getId()!=s.getContract().getId()){
			// Create a new entity if this id exists in another contract
			sectionEntity = new MasterSectionDraftEntity();
		}
		
		//final ContractEntity e = contractRepository.findById(s.getContract().getId()).get();
		sectionEntity.setContract(this.findContract(s.getContract().getId()).get());
		sectionEntity.setTitle(s.getTitle());
		sectionEntity.setIndex(s.getIndex());
		sectionEntity.setIndent(s.getIndent());
		sectionEntity.setVariable(s.isVariable());
		sectionEntity.setOptional(s.isOptional());
		sectionEntity.setDynamic(s.isDynamic());
		sectionEntity.setStyledOptions(s.getStyledOptions());
		sectionEntity.setOptions(s.getOptions());
		sectionEntity.setSuboptions(s.getSuboptions());
		sectionEntity.setSummary(s.getSummary());
		sectionEntity.setIcons(s.getIcons());
		return saveAndFlush(sectionEntity).toDto();
	}
	
	@Transactional(readOnly = false)
	default MasterSectionDraftDto saveFrom(MasterSectionDto s, MasterContractDraftEntity ce) {

		MasterSectionDraftEntity sectionEntity = null;
		sectionEntity = this.findById(s.getId()).orElse(null);
		if (sectionEntity == null) {
			
			// Create a new entity
			sectionEntity = new MasterSectionDraftEntity();
		}
		
		sectionEntity.setContract(ce);
		sectionEntity.setTitle(s.getTitle());
		sectionEntity.setIndex(s.getIndex());
		sectionEntity.setIndent(s.getIndent());
		sectionEntity.setVariable(s.isVariable());
		sectionEntity.setOptional(s.isOptional());
		sectionEntity.setDynamic(s.isDynamic());
		sectionEntity.setStyledOptions(s.getStyledOptions());
		sectionEntity.setOptions(s.getOptions());
		sectionEntity.setSuboptions(s.getSuboptions());
		sectionEntity.setSummary(s.getSummary());
		sectionEntity.setIcons(s.getIcons());
		return saveAndFlush(sectionEntity).toDto();
	}

	@Transactional(readOnly = false)
	default void remove(int id) {

		MasterSectionDraftEntity sectionEntity = this.findById(id).orElse(null);

		if (sectionEntity == null) {
			throw ApplicationException.fromMessage(
				BasicMessageCode.RecordNotFound, 
				"Record not found"
			);
		}

		this.deleteById(id);
	}

}
