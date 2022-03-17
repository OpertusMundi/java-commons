package eu.opertusmundi.common.service.contract;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.contract.ContractMessageCode;
import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.contract.ContractServiceException;
import eu.opertusmundi.common.model.contract.consumer.ConsumerContractCommand;
import eu.opertusmundi.common.repository.OrderRepository;

@Service
@Transactional
public class DefaultConsumerContractService implements ConsumerContractService {

    @Autowired
    private ContractFileManager contractFileManager;

    @Autowired
    private ContractParametersFactory contractParametersFactory;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
	private PdfContractGeneratorService pdfService;

	@Autowired(required = false)
    private SignPdfService signPdfService;

    @Override
    public void print(ConsumerContractCommand command) throws ContractServiceException {
        try {
            Assert.isNull(command.getPath(), "Expected a null contract path");

            final ContractParametersDto parameters = contractParametersFactory.create(command.getOrderKey());

            this.setPath(command, false);
            command.setDraft(true);

            if (!command.getPath().toFile().exists()) {
                final byte[] result = pdfService.renderConsumerPDF(parameters, command);

                // Save contract to file
                this.save(command.getPath(), result);
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

            Assert.state(signPdfService != null,
                "signPdfService is not present (is signatory keystore configured?)");

            final ContractParametersDto parameters = contractParametersFactory.create(command.getOrderKey());

            this.setPath(command, true);

            if (command.getPath().toFile().exists()) {
                return;
            }

            final byte[] pdfByteArray = pdfService.renderConsumerPDF(parameters, command);
            final int inputSize = pdfByteArray.length;
            final int estimatedOutputSize = (inputSize * 3) / 2;

            try (final ByteArrayOutputStream output = new ByteArrayOutputStream(estimatedOutputSize)) {
                final PDDocument pdf = PDDocument.load(pdfByteArray);

                signPdfService.signWithVisibleSignature(pdf, output);

                // Save signed contract to file
                this.save(command.getPath(), output.toByteArray());
            }

            this.orderRepository.setContractSignedDate(command.getOrderKey(), ZonedDateTime.now());

        } catch (final ContractServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new ContractServiceException(ContractMessageCode.ERROR, ex);
        }
    }

    private void setPath(ConsumerContractCommand command, boolean signed) {
        final Path path = contractFileManager.resolvePath(
            command.getUserId(),
            command.getOrderKey(),
            command.getItemIndex(),
            signed
        );

        command.setPath(path);
    }

    private void save(Path path, byte[] data) throws FileNotFoundException, IOException {
        try (final FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(data);
        }
    }

}
