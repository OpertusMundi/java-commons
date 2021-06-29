package eu.opertusmundi.common.service.contract;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.model.contract.ConsumerContractCommandDto;
import eu.opertusmundi.common.model.contract.ConsumerContractDto;
import eu.opertusmundi.common.model.contract.PrintConsumerContractCommand;
import eu.opertusmundi.common.model.contract.SignConsumerContractCommand;

@Service
@Transactional
public class DefaultConsumerContractService implements ConsumerContractService {

    @Override
    public Optional<ConsumerContractDto> findAssetContract(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConsumerContractDto createContract(ConsumerContractCommandDto command) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConsumerContractDto print(PrintConsumerContractCommand command) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConsumerContractDto sign(SignConsumerContractCommand command) {
        // TODO Auto-generated method stub
        return null;
    }

}
