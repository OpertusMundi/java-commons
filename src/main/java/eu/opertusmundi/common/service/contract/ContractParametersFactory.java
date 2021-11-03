package eu.opertusmundi.common.service.contract;

import java.util.UUID;

import eu.opertusmundi.common.model.contract.ContractParametersDto;

public interface ContractParametersFactory {

    ContractParametersDto create(UUID orderKey);
    
    ContractParametersDto createWithPlaceholderData();
}
