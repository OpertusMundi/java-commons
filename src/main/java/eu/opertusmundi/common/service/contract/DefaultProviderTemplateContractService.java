package eu.opertusmundi.common.service.contract;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.model.contract.PrintConsumerContractCommand;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractCommandDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractQuery;

@Service
@Transactional
public class DefaultProviderTemplateContractService implements ProviderTemplateContractService {

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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<ProviderTemplateContractDto> findOneByKey(UUID key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void update(ProviderTemplateContractCommandDto command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deactivateBy(int id) {
        // TODO Auto-generated method stub

    }

    @Override
    public byte[] print(PrintConsumerContractCommand command) {
        // TODO Auto-generated method stub
        return null;
    }

}
