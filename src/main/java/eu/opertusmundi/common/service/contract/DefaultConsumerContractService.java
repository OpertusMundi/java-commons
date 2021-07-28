package eu.opertusmundi.common.service.contract;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.contract.consumer.ConsumerContractCommandDto;
import eu.opertusmundi.common.model.contract.consumer.ConsumerContractDto;
import eu.opertusmundi.common.model.contract.consumer.PrintConsumerContractCommand;
import eu.opertusmundi.common.model.contract.consumer.SignConsumerContractCommand;

@Service
@Transactional
public class DefaultConsumerContractService implements ConsumerContractService {

	@Autowired
	private PdfContractGeneratorService pdfService;

	@Autowired
	private ContractFileManager contractFileManager;

	@Autowired
	private ContractOrderInformationService contractOrderInformationService;

    @Override
    public ConsumerContractDto createContract(ConsumerContractCommandDto command) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConsumerContractDto print(PrintConsumerContractCommand command) {
    	try {
    		final ContractParametersDto contractParameterDto	=	new ContractParametersDto();
//    		ContractParametersDto.Provider provider		= 	contractOrderInformationService.getProviderInformation(command.getOrderKey());
//    		ContractParametersDto.Consumer consumer		= 	contractOrderInformationService.getConsumerInformation(command.getOrderKey());
//    		ContractParametersDto.Product product		=	contractOrderInformationService.getProductInformation(command.getOrderKey());

    		final ContractParametersDto.Provider provider 	= 	new ContractParametersDto.Provider("Adaptas", "736 Jim Rosa Lane, San Francisco, CA 94108", "richardmsteffen@armyspy.com", "Richard M. Steffen", "012345678", "098765432");
    		final ContractParametersDto.Consumer consumer 	= 	new ContractParametersDto.Consumer("Life's Gold", "51, rue Adolphe Wurtz, 97420 LE PORT", "paulmstamper@teleworm.us", "Paul M. Stamper", "012345678", "632769332");
    		final ContractParametersDto.Product  product 		= 	new ContractParametersDto.Product("bdb87e25-4ac9-4a1e-85be-df4dced3d286", "Lakes of Greece", "Vector dataset with complete collection of the lakes in Greece", "Yes", "Yes", "Immediate", "csv file, digital download", "0%");

    		contractParameterDto.setProvider(provider);
    		contractParameterDto.setConsumer(consumer);
    		contractParameterDto.setProduct(product);

			final Path path = contractFileManager.resolvePath(
				command.getUserId(),
				command.getOrderKey(),
				command.getItemIndex(),
				false,
				false);

			if (!path.toFile().exists()) {
			    command.setPath(path);
				pdfService.renderPDF(contractParameterDto, command);
			} else {
				Files.readAllBytes(path);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
    	return null;
    }

    @Override
    public ConsumerContractDto sign(SignConsumerContractCommand command) {
        // TODO Auto-generated method stub
        return null;
    }

}
