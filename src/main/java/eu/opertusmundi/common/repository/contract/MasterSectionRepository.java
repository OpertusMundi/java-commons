package eu.opertusmundi.common.repository.contract;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.MasterContractEntity;
import eu.opertusmundi.common.domain.MasterSectionEntity;
import eu.opertusmundi.common.model.contract.MasterSectionDraftDto;
import eu.opertusmundi.common.model.contract.MasterSectionDto;
import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.BasicMessageCode;

@Repository
@Transactional(readOnly = true)
public interface MasterSectionRepository extends JpaRepository<MasterSectionEntity, Integer> {

	
	Optional<MasterSectionEntity> findOneById(Integer id);
	
	//Optional<SectionEntity> findOneByNameAndIdNot(String name, Integer id);

	//Page<SectionEntity> findAllByNameContains(Pageable pageable, String name);

	@Query("SELECT o FROM Section o WHERE :sectionId IS NULL OR o.id = :sectionId")
	List<MasterSectionEntity> findAll(
		@Param("sectionId") Integer sectionId,
		Sort sort
	);
	
	@Query("SELECT c FROM Contract c WHERE c.id = :contractId")
    Optional<MasterContractEntity> findContract(@Param("contractId") Integer contractId);
	
	@Transactional(readOnly = false)
	default MasterSectionDto saveFrom(MasterSectionDto s) {

		MasterSectionEntity sectionEntity = null;
		sectionEntity = this.findById(s.getId()).orElse(null);
		if (sectionEntity == null) {
			
			// Create a new entity
			sectionEntity = new MasterSectionEntity();
		}
		else if(sectionEntity.getContract().getId()!=s.getContract().getId()){
			// Create a new entity if this id exists in another contract
			sectionEntity = new MasterSectionEntity();
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
	default MasterSectionDto saveFrom(MasterSectionDraftDto s, MasterContractEntity ce) {

		MasterSectionEntity sectionEntity = null;
		sectionEntity = this.findById(s.getId()).orElse(null);
		if (sectionEntity == null) {
			
			// Create a new entity
			sectionEntity = new MasterSectionEntity();
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

		MasterSectionEntity sectionEntity = this.findById(id).orElse(null);

		if (sectionEntity == null) {
			throw ApplicationException.fromMessage(
				BasicMessageCode.RecordNotFound, 
				"Record not found"
			);
		}

		this.deleteById(id);
	}

}
