package eu.opertusmundi.common.service.contract;

import java.io.IOException;

import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.contract.EnumContract;
import eu.opertusmundi.common.model.contract.consumer.PrintConsumerContractCommand;

/**
 * OpertusMundi PDF Contract Generator
 */
public interface PdfContractGeneratorService {
	
	
	public void init() throws IOException;
	
	 /**
     * Create a new PDF contract
     *
     * @param 
     * @return
     */
	public byte[] renderPDF(ContractParametersDto contractParametersDto, PrintConsumerContractCommand command, String filePath) throws IOException;

}
