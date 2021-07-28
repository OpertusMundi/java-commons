package eu.opertusmundi.common.service.contract;

import java.io.IOException;

import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.contract.consumer.PrintConsumerContractCommand;

public interface PdfContractGeneratorService {

    /**
     * Render contract PDF
     *
     * @param contractParametersDto
     * @param command
     * @return
     * @throws IOException
     */
    byte[] renderPDF(ContractParametersDto contractParametersDto, PrintConsumerContractCommand command) throws IOException;

}
