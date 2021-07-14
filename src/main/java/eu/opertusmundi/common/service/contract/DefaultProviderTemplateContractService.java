package eu.opertusmundi.common.service.contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.ProviderTemplateContractDraftEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractHistoryEntity;
import eu.opertusmundi.common.domain.ProviderTemplateSectionDraftEntity;
import eu.opertusmundi.common.domain.ProviderTemplateSectionEntity;
import eu.opertusmundi.common.model.contract.PrintConsumerContractCommand;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractCommandDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractDraftDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractHistoryDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractQuery;
import eu.opertusmundi.common.model.contract.ProviderTemplateSectionDraftDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateSectionDto;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractDraftRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractHistoryRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateSectionDraftRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateSectionHistoryRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateSectionRepository;

@Service
@Transactional
public class DefaultProviderTemplateContractService implements ProviderTemplateContractService {

	@Autowired
	private ProviderTemplateContractRepository providerContractRepository;

	@Autowired
	private ProviderTemplateContractDraftRepository providerContractDraftRepository;

	@Autowired
	private ProviderTemplateContractHistoryRepository providerContractHistoryRepository;

	@Autowired
	private ProviderTemplateSectionRepository providerSectionRepository;

	@Autowired
	private ProviderTemplateSectionDraftRepository providerSectionDraftRepository;

	@Autowired
	private ProviderTemplateSectionHistoryRepository providerSectionHistoryRepository;

	@Override
	public ProviderTemplateContractDraftDto createDraft(ProviderTemplateContractDraftDto providerDraftDto) {
		List<ProviderTemplateSectionDraftDto> sections = providerDraftDto.getSections();
		providerDraftDto.setSections(null);
		ProviderTemplateContractDraftDto resultRecord = providerContractDraftRepository.saveFrom(providerDraftDto);
		// create sections
		final ProviderTemplateContractDraftEntity e = providerContractDraftRepository.findById(resultRecord.getId()).get();
		providerDraftDto.setId(e.getId());
		providerDraftDto.setParentId(e.getId());
		providerContractDraftRepository.saveFrom(providerDraftDto);
		for (ProviderTemplateSectionDraftDto s : sections) {
			s.setContract(providerDraftDto);
			providerSectionDraftRepository.saveFrom(s);
		}

		return providerContractDraftRepository.saveFrom(providerDraftDto);

	}

	@Override
	public List<ProviderTemplateContractDto> findAll(ProviderTemplateContractQuery query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> findAllAssignedAssets(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<ProviderTemplateContractDto> findOneByAsset(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<ProviderTemplateContractDto> findOneById(int id) {
		final Optional<ProviderTemplateContractEntity> contractEntity = providerContractRepository.findById(id);

		return Optional.of(contractEntity.get().toDto());
	}

	@Override
	public Optional<ProviderTemplateContractDto> findOneByKey(UUID key) {
		final Optional<ProviderTemplateContractEntity> contractEntity = providerContractRepository.findByKey(key);

		return Optional.of(contractEntity.get().toDto());
	}

	@Override
	public void update(ProviderTemplateContractCommandDto commanb) {

	}

	@Override
	public void deactivateBy(int id) {
		final Optional<ProviderTemplateContractEntity> contractEntity = providerContractRepository.findById(id);
		contractEntity.get().setActive(false);
		providerContractRepository.saveFrom(contractEntity.get().toDto());

	}

	@Override
	public byte[] print(PrintConsumerContractCommand command) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateDraft(ProviderTemplateContractDraftDto draftDto) {
		List<ProviderTemplateSectionDraftDto> sections = draftDto.getSections();
		draftDto.setSections(null);
		ProviderTemplateContractDraftDto resultRecord = providerContractDraftRepository.saveFrom(draftDto);
		resultRecord = providerContractDraftRepository.saveFrom(draftDto);

		final ProviderTemplateContractDraftEntity e = providerContractDraftRepository.findById(resultRecord.getId()).get();
		draftDto.setId(e.getId());
		
		//List<SectionEntity> contractSections = contractRepository.findSectionsByContract(e);
		List<Integer> newSectionIds = new ArrayList<Integer>(); 
		int newId;
		for (ProviderTemplateSectionDraftDto s : sections){
			s.setContract(draftDto);
			newId =  providerSectionDraftRepository.saveFrom(s).getId();
			newSectionIds.add(newId);
		}
		
		List<Integer> prevSectionIds = providerContractDraftRepository.findSectionsIdsByContract(e);
		List<Integer> differences = new ArrayList<>(prevSectionIds);
		differences.removeAll(newSectionIds);
		for (Integer i: differences) {
			providerSectionDraftRepository.remove(i);
		}
		
	}
	
	@Override
	public void updateState(int id, String state) {
		if (state.equals("DRAFT")) {
			final ProviderTemplateContractEntity ce = providerContractRepository.findById(id).orElse(null);

			if (ce == null) {
				return;
			}
			
			//save contract in draft
			ProviderTemplateContractDraftDto cDto = providerContractDraftRepository.saveFrom(ce.toDto());
			
			// save sections
			List<ProviderTemplateSectionEntity> sections = providerContractRepository.findSectionsByContract(ce);
			for (ProviderTemplateSectionEntity s : sections){
				providerSectionDraftRepository.saveFrom(s.toDto(), providerContractDraftRepository.findById(cDto.getId()).get() );
			} 
			
		}
		else {
			final ProviderTemplateContractDraftEntity ce = providerContractDraftRepository.findById(id).orElse(null);
			if (ce == null) {
				return;
			}
			// remove old published contract
			final ProviderTemplateContractEntity ceOld = providerContractRepository.findByParentId(ce.getParentId()).orElse(null);
			if(ceOld != null) {
				this.delete(ceOld.getId());
			}
			// increment version
			ce.setVersion("" + (Integer.parseInt(ce.getVersion())+1));
			ce.setActive(true);
			
			//save contract in history table
			ProviderTemplateContractHistoryDto contractHistoryDto = providerContractHistoryRepository.saveFrom(ce.toDto());
			
			//save contract in published
			ProviderTemplateContractDto cDto = providerContractRepository.saveFrom(ce.toDto());
			
			// save sections
			List<ProviderTemplateSectionDraftEntity> sections = providerContractRepository.findDraftSectionsByContract(ce);
			for (ProviderTemplateSectionDraftEntity s : sections){
				providerSectionRepository.saveFrom(s.toDto(), providerContractRepository.findById(cDto.getId()).get() );
			} 
			
			
			ProviderTemplateContractHistoryEntity contractHistoryEntity = providerContractHistoryRepository.findById(contractHistoryDto.getId()).get();
			
			//save sections in history table
			for (ProviderTemplateSectionDraftEntity s : sections){
				providerSectionHistoryRepository.saveFrom(s.toDto(), contractHistoryEntity);
			} 
			//remove from drafts
			this.deleteDraft(id);
		}
	}
	
	@Override
	public void deleteDraft(int id) {
		final ProviderTemplateContractDraftEntity ce = providerContractDraftRepository.findById(id).orElse(null);

		if (ce == null) {
			return;
		}
		List<ProviderTemplateSectionDraftEntity> sections = providerContractDraftRepository.findSectionsByContract(ce);
		for (ProviderTemplateSectionDraftEntity s : sections){
			providerSectionDraftRepository.remove(s.getId());
		}
	
		providerContractDraftRepository.remove(id);

	}
	
	@Override
	public void delete(int id) {
		final ProviderTemplateContractEntity ce = providerContractRepository.findById(id).orElse(null);

		if (ce == null) {
			return;
		}
		List<ProviderTemplateSectionEntity> sections = providerContractRepository.findSectionsByContract(ce);
		for (ProviderTemplateSectionEntity s : sections){
			providerSectionRepository.remove(s.getId());
		}

		providerContractRepository.remove(id);

	}

}
