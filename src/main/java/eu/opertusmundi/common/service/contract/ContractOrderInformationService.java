package eu.opertusmundi.common.service.contract;

import java.util.UUID;

import eu.opertusmundi.common.model.contract.ContractParametersDto.Consumer;
import eu.opertusmundi.common.model.contract.ContractParametersDto.Product;
import eu.opertusmundi.common.model.contract.ContractParametersDto.Provider;

public interface ContractOrderInformationService {

	Consumer getConsumerInformation(UUID orderKey);
	
	Provider getProviderInformation(UUID orderKey);
	
	Product getProductInformation(UUID orderKey);
	
}
