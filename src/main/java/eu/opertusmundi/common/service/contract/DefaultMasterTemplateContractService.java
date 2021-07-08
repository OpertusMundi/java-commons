package eu.opertusmundi.common.service.contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.model.contract.MasterContractQueryDto;
import eu.opertusmundi.common.domain.MasterContractEntity;
import eu.opertusmundi.common.model.contract.MasterContractCommandDto;
import eu.opertusmundi.common.model.contract.MasterContractDto;
import eu.opertusmundi.common.model.contract.PrintConsumerContractCommand;
import eu.opertusmundi.common.repository.contract.MasterContractRepository;

@Service
@Transactional
public class DefaultMasterTemplateContractService implements MasterTemplateContractService {

	
	@Autowired
    private MasterContractRepository masterContractRepository;

    @Override
    public List<MasterContractDto> findAll(MasterContractQueryDto query) {
    	List<MasterContractDto> contractDtos = new ArrayList<MasterContractDto>();
    	List<MasterContractEntity> contracts = masterContractRepository.findAll();
    	
    	for( MasterContractEntity contract : contracts) {
    		if (query.getActive() && contract.getActive()) {
    			contractDtos.add(contract.toDto());
    		}
    	}
    	return contractDtos;
    }

    @Override
    public Optional<MasterContractDto> findOneById(int id) {
    	Optional<MasterContractEntity> contractEntity = masterContractRepository.findById(id);
    	return Optional.of(contractEntity.get().toDto());
    	
    }

    @Override
    public Optional<MasterContractDto> findOneByKey(UUID key) {
    	
    	Optional<MasterContractEntity> contractEntity = masterContractRepository.findByKey(key);
    	return Optional.of(contractEntity.get().toDto());
    }

    @Override
    public void update(MasterContractCommandDto command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deactivateBy(int id) {
    	Optional<MasterContractEntity> contractEntity = masterContractRepository.findById(id);
    	contractEntity.get().setActive(false);
    	masterContractRepository.saveFrom(contractEntity.get().toDto());
    }

    @Override
    public byte[] print(PrintConsumerContractCommand command) {
        // TODO Auto-generated method stub
        return null;
    }

}
