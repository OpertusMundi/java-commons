package eu.opertusmundi.common.service.contract;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import eu.opertusmundi.common.model.contract.ContractMessageCode;
import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.contract.ContractServiceException;
import eu.opertusmundi.common.model.contract.consumer.ConsumerContractCommand;
import eu.opertusmundi.common.model.order.HelpdeskOrderDto;
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

            final Path contractPath = contractFileManager.resolveMasterContractPath(
                command.getUserId(),
                command.getOrderKey(),
                command.getItemIndex(),
                false
            );
            command.setPath(contractPath);
            command.setDraft(true);

            if (!command.getPath().toFile().exists()) {
                final ContractParametersDto parameters = contractParametersFactory.create(command.getOrderKey());
                final byte[]                result     = pdfService.renderConsumerPDF(parameters, command);

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
            Assert.state(signPdfService != null, "signPdfService is not present (is signatory keystore configured?)");

            final HelpdeskOrderDto order = orderRepository.findOrderObjectByKey(command.getOrderKey()).orElse(null);

            Assert.notNull(order, "Expected a non-null order");
            Assert.isTrue(order.getItems().size() == 1, "Expected a single order item");

            final EnumContractType type = order.getItems().get(0).getContractType();

            switch (type) {
                case MASTER_CONTRACT :
                    this.signPlatformContract(command);
                    break;

                case UPLOADED_CONTRACT :
                    this.signUploadedContract(command);
                    break;

                case OPEN_DATASET :
                    // No action is required
                    break;
            }

            if (type != EnumContractType.OPEN_DATASET) {
                this.orderRepository.setContractSignedDate(command.getOrderKey(), ZonedDateTime.now());
            }

        } catch (final ContractServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new ContractServiceException(ContractMessageCode.ERROR, ex);
        }
    }

    private void signPlatformContract(ConsumerContractCommand command) throws Exception {
        final ContractParametersDto parameters   = contractParametersFactory.create(command.getOrderKey());
        final Path                  contractPath = contractFileManager.resolveMasterContractPath(
            command.getUserId(),
            command.getOrderKey(),
            command.getItemIndex(),
            true
        );
        command.setPath(contractPath);

        if (command.getPath().toFile().exists()) {
            return;
        }

        final byte[] pdfByteArray        = pdfService.renderConsumerPDF(parameters, command);
        final int    inputSize           = pdfByteArray.length;
        final int    estimatedOutputSize = (inputSize * 3) / 2;

        try (final ByteArrayOutputStream output = new ByteArrayOutputStream(estimatedOutputSize)) {
            final PDDocument pdf = PDDocument.load(pdfByteArray);

            signPdfService.signWithVisibleSignature(pdf, output);

            // Save signed contract to file
            this.save(command.getPath(), output.toByteArray());
        }
    }

    private void signUploadedContract(ConsumerContractCommand command) throws Exception {
        final Path initialContractPath = contractFileManager.resolveUploadedContractPath(
            command.getUserId(),
            command.getOrderKey(),
            command.getItemIndex(),
            false
        );

        Assert.notNull(initialContractPath, "Expected a non-null uploaded contract path");
        Assert.isTrue(initialContractPath.toFile().exists(), "Expected uploaded contract file to exist");

        final Path signedContractPath = contractFileManager.resolveUploadedContractPath(
            command.getUserId(),
            command.getOrderKey(),
            command.getItemIndex(),
            true
        );
        command.setPath(signedContractPath);

        if (command.getPath().toFile().exists()) {
            return;
        }

        final byte[] pdfByteArray        = Files.readAllBytes(initialContractPath);
        final int    inputSize           = pdfByteArray.length;
        final int    estimatedOutputSize = (inputSize * 3) / 2;

        try (final ByteArrayOutputStream output = new ByteArrayOutputStream(estimatedOutputSize)) {
            final PDDocument pdf = PDDocument.load(pdfByteArray);

            signPdfService.signWithVisibleSignature(pdf, output);

            // Save signed contract to file
            this.save(command.getPath(), output.toByteArray());
        }
    }

    private void save(Path path, byte[] data) throws FileNotFoundException, IOException {
        try (final FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(data);
        }
    }

}
