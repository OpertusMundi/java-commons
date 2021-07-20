package eu.opertusmundi.common.service.contract;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.model.contract.consumer.ConsumerContractCommandDto;
import eu.opertusmundi.common.model.contract.consumer.ConsumerContractDto;
import eu.opertusmundi.common.model.contract.consumer.PrintConsumerContractCommand;
import eu.opertusmundi.common.model.contract.consumer.SignConsumerContractCommand;

@Service
@Transactional
public class DefaultConsumerContractService implements ConsumerContractService {

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
