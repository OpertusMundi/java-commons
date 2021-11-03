package eu.opertusmundi.common.service.contract;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.contract.ContractMessageCode;
import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.contract.ContractServiceException;
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
            Assert.isNull(command.getPath(), "Expected a null contract path");

            final ContractParametersDto parameters = contractParametersFactory.create(command.getOrderKey());

			final Path path = contractFileManager.resolvePath(
				command.getUserId(),
				command.getOrderKey(),
				command.getItemIndex(),
				false,
				false);

            command.setPath(path);

            if (!path.toFile().exists()) {
                pdfService.renderConsumerPDF(parameters, command);
            }
        } catch (final ContractServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new ContractServiceException(ContractMessageCode.ERROR, ex);
        }
    }

    @Override
    public void sign(SignConsumerContractCommand command) {
        try {
            Assert.isNull(command.getSourcePath(), "Expected a null source path");
            Assert.isNull(command.getTargetPath(), "Expected a null target path");

            final Path sourcePath = this.contractFileManager.resolvePath(
                command.getUserId(),
                command.getOrderKey(),
                command.getItemIndex(),
                false, true
            );
            final Path targetPath = this.contractFileManager.resolvePath(
                command.getUserId(),
                command.getOrderKey(),
                command.getItemIndex(),
                true, false
            );

            command.setSourcePath(sourcePath);
            command.setTargetPath(targetPath);

            // TODO Auto-generated method stub
            // TODO Remove assignment. Used only as placeholder
            command.setTargetPath(sourcePath);
        } catch (final ContractServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new ContractServiceException(ContractMessageCode.ERROR, ex);
        }
    }

}
