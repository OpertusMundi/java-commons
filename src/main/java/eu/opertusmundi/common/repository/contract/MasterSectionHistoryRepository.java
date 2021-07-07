package eu.opertusmundi.common.repository.contract;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.MasterContractHistoryEntity;
import eu.opertusmundi.common.domain.MasterSectionHistoryEntity;
import eu.opertusmundi.common.model.contract.MasterSectionDraftDto;
import eu.opertusmundi.common.model.contract.MasterSectionDto;
import eu.opertusmundi.common.model.contract.MasterSectionHistoryDto;
import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.BasicMessageCode;

@Repository
@Transactional(readOnly = true)
public interface MasterSectionHistoryRepository extends JpaRepository<MasterSectionHistoryEntity, Integer> {

	
	Optional<MasterSectionHistoryEntity> findOneById(Integer id);
	
	//Optional<SectionEntity> findOneByNameAndIdNot(String name, Integer id);

	//Page<SectionEntity> findAllByNameContains(Pageable pageable, String name);

	@Query("SELECT o FROM SectionHistory o WHERE :sectionId IS NULL OR o.id = :sectionId")
	List<MasterSectionHistoryEntity> findAll(
		@Param("sectionId") Integer sectionId,
		Sort sort
	);
	
	@Query("SELECT s FROM SectionHistory s WHERE s.contract = :contract")
    Optional<MasterSectionHistoryEntity> findSectionHistoryEntity(
    		MasterContractHistoryEntity contract);
	
	@Query("SELECT c FROM ContractHistory c WHERE c.id = :contractId")
    Optional<MasterContractHistoryEntity> findContract(@Param("contractId") Integer contractId);
	
	@Transactional(readOnly = false)
	default MasterSectionHistoryDto saveFrom(MasterSectionDto s, MasterContractHistoryEntity contract) {
		

		MasterSectionHistoryEntity sectionHistoryEntity = null;
			
		// Create a new entity
		sectionHistoryEntity = new MasterSectionHistoryEntity();
		
		//final ContractEntity e = contractRepository.findById(s.getContract().getId()).get();
		sectionHistoryEntity.setContract(contract);
		sectionHistoryEntity.setTitle(s.getTitle());
		sectionHistoryEntity.setIndex(s.getIndex());
		sectionHistoryEntity.setIndent(s.getIndent());
		sectionHistoryEntity.setVariable(s.isVariable());
		sectionHistoryEntity.setOptional(s.isOptional());
		sectionHistoryEntity.setDynamic(s.isDynamic());
		sectionHistoryEntity.setStyledOptions(s.getStyledOptions());
		sectionHistoryEntity.setOptions(s.getOptions());
		sectionHistoryEntity.setSummary(s.getSummary());
		sectionHistoryEntity.setIcons(s.getIcons());
		return saveAndFlush(sectionHistoryEntity).toDto();
	}
	
	@Transactional(readOnly = false)
	default MasterSectionHistoryDto saveFrom(MasterSectionDraftDto s, MasterContractHistoryEntity contract) {
		

		MasterSectionHistoryEntity sectionHistoryEntity = null;
			
		// Create a new entity
		sectionHistoryEntity = new MasterSectionHistoryEntity();
		
		sectionHistoryEntity.setContract(contract);
		sectionHistoryEntity.setTitle(s.getTitle());
		sectionHistoryEntity.setIndex(s.getIndex());
		sectionHistoryEntity.setIndent(s.getIndent());
		sectionHistoryEntity.setVariable(s.isVariable());
		sectionHistoryEntity.setOptional(s.isOptional());
		sectionHistoryEntity.setDynamic(s.isDynamic());
		sectionHistoryEntity.setStyledOptions(s.getStyledOptions());
		sectionHistoryEntity.setOptions(s.getOptions());
		sectionHistoryEntity.setSummary(s.getSummary());
		sectionHistoryEntity.setIcons(s.getIcons());
		return saveAndFlush(sectionHistoryEntity).toDto();
	}

	@Transactional(readOnly = false)
	default void remove(int id) {

		MasterSectionHistoryEntity sectionHistoryEntity = this.findById(id).orElse(null);

		if (sectionHistoryEntity == null) {
			throw ApplicationException.fromMessage(
				BasicMessageCode.RecordNotFound, 
				"Record not found"
			);
		}

		this.deleteById(id);
	}

}
