package eu.opertusmundi.common.service.contract;

import java.io.IOException;

import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.contract.consumer.ConsumerContractCommand;
import eu.opertusmundi.common.model.contract.provider.ProviderContractCommand;

public interface PdfContractGeneratorService {

    /**
     * Render consumer contract PDF
     *
     * @param contractParametersDto
     * @param command
     * @return
     * @throws IOException
     */
    byte[] renderConsumerPDF(ContractParametersDto contractParametersDto, ConsumerContractCommand command) throws IOException;

    /**
     * Render provider contract PDF
     *
     * @param contractParametersDto
     * @param command
     * @return
     * @throws IOException
     */
    byte[] renderProviderPDF(ContractParametersDto contractParametersDto, ProviderContractCommand command) throws IOException;
    
    /**
     * Render master contract PDF
     *
     * @param masterContractKey
     * @return
     * @throws IOException
     */
    byte[] renderMasterPDF(ContractParametersDto contractParametersDto, int masterContractId) throws IOException;

}
