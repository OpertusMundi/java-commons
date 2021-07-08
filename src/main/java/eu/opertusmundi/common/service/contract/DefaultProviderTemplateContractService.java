package eu.opertusmundi.common.service.contract;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.MasterContractEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractEntity;
import eu.opertusmundi.common.model.contract.PrintConsumerContractCommand;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractCommandDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractQuery;
import eu.opertusmundi.common.repository.contract.MasterContractRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractRepository;

@Service
@Transactional
public class DefaultProviderTemplateContractService implements ProviderTemplateContractService {


	@Autowired
    private ProviderTemplateContractRepository providerContractRepository;
	
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
    	Optional<ProviderTemplateContractEntity> contractEntity = providerContractRepository.findById(id);
    	
    	return Optional.of(contractEntity.get().toDto());
    }

    @Override
    public Optional<ProviderTemplateContractDto> findOneByKey(UUID key) {
    	Optional<ProviderTemplateContractEntity> contractEntity = providerContractRepository.findByKey(key);
    	
    	return Optional.of(contractEntity.get().toDto());
    }

    @Override
    public void update(ProviderTemplateContractCommandDto command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deactivateBy(int id) {
    	Optional<ProviderTemplateContractEntity> contractEntity = providerContractRepository.findById(id);
    	contractEntity.get().setActive(false);
    	providerContractRepository.saveFrom(contractEntity.get().toDto());

    }

    @Override
    public byte[] print(PrintConsumerContractCommand command) {
        // TODO Auto-generated method stub
        return null;
    }

}
