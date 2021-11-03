package eu.opertusmundi.common.service.contract;

import java.io.IOException;

import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.contract.consumer.PrintConsumerContractCommand;
import eu.opertusmundi.common.model.contract.provider.PrintProviderContractCommand;

public interface PdfContractGeneratorService {

    /**
     * Render contract PDF
     *
     * @param contractParametersDto
     * @param command
     * @return
     * @throws IOException
     */

    public byte[] renderConsumerPDF(ContractParametersDto contractParametersDto, PrintConsumerContractCommand command) throws IOException;
    
    public byte[] renderProviderPDF(ContractParametersDto contractParametersDto, PrintProviderContractCommand command) throws IOException;
    
}
