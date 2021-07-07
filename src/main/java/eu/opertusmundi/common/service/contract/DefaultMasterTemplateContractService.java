package eu.opertusmundi.common.service.contract;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.model.contract.MasterContractQueryDto;
import eu.opertusmundi.common.model.contract.MasterContractCommandDto;
import eu.opertusmundi.common.model.contract.MasterContractDto;
import eu.opertusmundi.common.model.contract.PrintConsumerContractCommand;

@Service
@Transactional
public class DefaultMasterTemplateContractService implements MasterTemplateContractService {

    @Override
    public List<MasterContractDto> findAll(MasterContractQueryDto query) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<MasterContractDto> findOneById(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<MasterContractDto> findOneByKey(UUID key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void update(MasterContractCommandDto command) {
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
