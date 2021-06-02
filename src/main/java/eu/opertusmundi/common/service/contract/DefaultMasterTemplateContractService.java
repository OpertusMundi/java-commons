package eu.opertusmundi.common.service.contract;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.model.contract.MasterContractQueryDto;
import eu.opertusmundi.common.model.contract.MasterTemplateContractCommandDto;
import eu.opertusmundi.common.model.contract.MasterTemplateContractDto;
import eu.opertusmundi.common.model.contract.PrintContractCommandDto;

@Service
@Transactional
public class DefaultMasterTemplateContractService implements MasterTemplateContractService {

    @Override
    public List<MasterTemplateContractDto> findAll(MasterContractQueryDto query) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<MasterTemplateContractDto> findOneById(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<MasterTemplateContractDto> findOneByKey(UUID key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void update(MasterTemplateContractCommandDto command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deactivateBy(int id) {
        // TODO Auto-generated method stub

    }

    @Override
    public byte[] print(PrintContractCommandDto command) {
        // TODO Auto-generated method stub
        return null;
    }

}
