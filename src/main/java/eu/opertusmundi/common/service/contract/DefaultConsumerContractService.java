package eu.opertusmundi.common.service.contract;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.model.contract.ContractMessageCode;
import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.contract.ContractServiceException;
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
	private ContractParametersFactory contractParametersFactory;

    @Override
    public void print(PrintConsumerContractCommand command) throws ContractServiceException {
        try {
            final ContractParametersDto parameters = contractParametersFactory.create(command.getOrderKey());

			final Path path = contractFileManager.resolvePath(
				command.getUserId(),
				command.getOrderKey(),
				command.getItemIndex(),
				false,
				false);

            command.setPath(path);

            if (!path.toFile().exists()) {
                pdfService.renderPDF(parameters, command);
            }
        } catch (final ContractServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new ContractServiceException(ContractMessageCode.ERROR, ex);
        }
    }

    @Override
    public ConsumerContractDto sign(SignConsumerContractCommand command) {
        // TODO Auto-generated method stub
        return null;
    }

}
