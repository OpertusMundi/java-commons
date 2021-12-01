package eu.opertusmundi.common.service.contract;

import java.io.IOException;

import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.contract.consumer.ConsumerContractCommand;
import eu.opertusmundi.common.model.contract.provider.ProviderContractCommand;

public interface PdfContractGeneratorService {

    /**
     * Render contract PDF
     *
     * @param contractParametersDto
     * @param command
     * @return
     * @throws IOException
     */

    public byte[] renderConsumerPDF(ContractParametersDto contractParametersDto, ConsumerContractCommand command) throws IOException;
    
    public byte[] renderProviderPDF(ContractParametersDto contractParametersDto, ProviderContractCommand command) throws IOException;
    
}
