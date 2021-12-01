package eu.opertusmundi.common.service.contract;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.contract.ContractMessageCode;
import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.contract.ContractServiceException;
import eu.opertusmundi.common.model.contract.consumer.ConsumerContractCommand;
import eu.opertusmundi.common.service.contract.SignPdfService;

@Service
@Transactional
public class DefaultConsumerContractService implements ConsumerContractService {

	@Autowired
	private PdfContractGeneratorService pdfService;

	@Autowired
	private ContractFileManager contractFileManager;

	@Autowired
	private ContractParametersFactory contractParametersFactory;
	
	@Autowired
    private SignPdfService signatoryService;

    @Override
    public void print(ConsumerContractCommand command) throws ContractServiceException {
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
    public void sign(ConsumerContractCommand command) {
        try {
            Assert.isNull(command.getPath(), "Expected a null source path");

            final ContractParametersDto parameters = contractParametersFactory.create(command.getOrderKey());

            final Path path = this.contractFileManager.resolvePath(
                command.getUserId(),
                command.getOrderKey(),
                command.getItemIndex(),
                false, true
            );
            
            command.setPath(path);
            
            final byte[] pdfByteArray = pdfService.renderConsumerPDF(parameters, command);
            final int inputSize = (int) pdfByteArray.length;
            final int estimatedOutputSize = (inputSize * 3) / 2;
            
            final ByteArrayOutputStream output = new ByteArrayOutputStream(estimatedOutputSize);
            
            PDDocument pdf = PDDocument.load(pdfByteArray);
            
            signatoryService.signWithVisibleSignature(pdf, output);
            
            /* save signed contract to file */
        	try (FileOutputStream fos = new FileOutputStream(command.getPath().toString())) {
                fos.write(output.toByteArray());
            }
            
        } catch (final ContractServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new ContractServiceException(ContractMessageCode.ERROR, ex);
        }
    }

}
