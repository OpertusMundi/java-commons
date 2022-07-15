package eu.opertusmundi.common.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.AssetContractAnnexEntity;
import eu.opertusmundi.common.domain.MasterSectionHistoryEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractHistoryEntity;
import eu.opertusmundi.common.model.asset.AssetContractAnnexDto;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.contract.ContractTermDto;
import eu.opertusmundi.common.model.contract.CustomContractDto;
import eu.opertusmundi.common.model.contract.TemplateContractDto;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.repository.AssetContractAnnexRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractHistoryRepository;
import eu.opertusmundi.common.service.DraftFileManager;
import eu.opertusmundi.common.service.QuotationService;
import eu.opertusmundi.common.service.integration.DataProviderManager;

@Service
public class CatalogueItemUtils {

    private static final Logger logger = LoggerFactory.getLogger(CatalogueItemUtils.class);

    @Value("${opertusmundi.contract.icons}")
    private String iconFolder;

    private final DataProviderManager dataProviderManager;

    private final QuotationService quotationService;

    private final ResourceLoader resourceLoader;

    private final AssetContractAnnexRepository assetContractAnnexRepository;

    private final ProviderTemplateContractHistoryRepository providerContractRepository;

    private final DraftFileManager draftFileManager;

    @Autowired
    public CatalogueItemUtils(
        AssetContractAnnexRepository assetContractAnnexRepository,
        DataProviderManager dataProviderManager,
        DraftFileManager draftFileManager,
        ProviderTemplateContractHistoryRepository providerContractRepository,
        QuotationService quotationService,
        ResourceLoader resourceLoader
    ) {
        this.assetContractAnnexRepository = assetContractAnnexRepository;
        this.dataProviderManager          = dataProviderManager;
        this.draftFileManager             = draftFileManager;
        this.providerContractRepository   = providerContractRepository;
        this.quotationService             = quotationService;
        this.resourceLoader               = resourceLoader;
    }

    /**
     * Compute pricing models effective values for a catalogue item
     *
     * @param item
     */
    public void refreshPricingModels(CatalogueItemDto item) {
        Assert.notNull(item, "Expected a non-null catalogue item");

        // Inject pricing models from external data providers
        dataProviderManager.updatePricingModels(item);

        final List<BasePricingModelCommandDto> models = item.getPricingModels();

        if (models.isEmpty()) {
            return;
        }

        final List<EffectivePricingModelDto> quotations = quotationService.createQuotation(item);

        item.setEffectivePricingModels(quotations);
    }

    public void setContract(CatalogueItemDetailsDto item, CatalogueFeature feature) {
        switch (item.getContractTemplateType()) {
            case MASTER_CONTRACT :
                this.setProviderContract(item, feature);
                break;

            case UPLOADED_CONTRACT :
                this.setCustomContract(item, feature);
                break;

            case OPEN_DATASET :
                // No action is required
                break;
        }
    }

    private void setProviderContract(CatalogueItemDetailsDto item, CatalogueFeature feature) {
        Assert.notNull(item, "Expected a non-null item");
        Assert.notNull(feature, "Expected a non-null feature");
        Assert.isTrue(item.getContractTemplateType() == EnumContractType.MASTER_CONTRACT, "Expected contract type to be MASTER_CONTRACT");

        // Find contract template using the template identifier and version from
        // catalogue item
        final ProviderTemplateContractHistoryEntity providerTemplate = this.providerContractRepository.findByIdAndVersion(
            feature.getProperties().getPublisherId(),
            feature.getProperties().getContractTemplateId(),
            feature.getProperties().getContractTemplateVersion()
        ).orElse(null);

        if (providerTemplate != null) {
            final TemplateContractDto contract = providerTemplate.toSimpleDto();

            // Inject contract terms and conditions
            setContractTermsAndConditions(providerTemplate, contract);

            item.setContract(contract);
        }
    }

    private void setCustomContract(CatalogueItemDetailsDto item, CatalogueFeature feature) {
        Assert.notNull(item, "Expected a non-null item");
        Assert.notNull(feature, "Expected a non-null feature");
        Assert.isTrue(item.getContractTemplateType() == EnumContractType.UPLOADED_CONTRACT, "Expected contract type to be UPLOADED_CONTRACT");

        final List<AssetContractAnnexDto> annexes = this.assetContractAnnexRepository.findAllAnnexesByAssetPid(item.getId()).stream()
            .map(AssetContractAnnexEntity::toDto)
            .collect(Collectors.toList());

        final CustomContractDto contract = new CustomContractDto(annexes);

        item.setContract(contract);
    }

    public void setContract(CatalogueItemDetailsDto item, AssetDraftDto draft) {
        switch (item.getContractTemplateType()) {
            case MASTER_CONTRACT :
                this.setProviderContract(item, draft);
                break;

            case UPLOADED_CONTRACT :
                this.setCustomContract(item, draft);
                break;

            case OPEN_DATASET :
                // No action is required
                break;
        }
    }

    private void setProviderContract(CatalogueItemDetailsDto item, AssetDraftDto draft) {
        Assert.notNull(item, "Expected a non-null item");
        Assert.notNull(draft, "Expected a non-null draft");
        Assert.isTrue(item.getContractTemplateType() == EnumContractType.MASTER_CONTRACT, "Expected contract type to be MASTER_CONTRACT");

        // Find contract template using the template key from draft
        final ProviderTemplateContractHistoryEntity providerTemplate = this.providerContractRepository.findByKey(
            draft.getPublisher().getKey(),
            draft.getCommand().getContractTemplateKey()
        ).orElse(null);

        final TemplateContractDto contract = providerTemplate.toSimpleDto();

        // Set template id and version to item
        item.setContractTemplateId(contract.getId());
        item.setContractTemplateVersion(contract.getVersion());

        // Inject contract terms and conditions
        setContractTermsAndConditions(providerTemplate, contract);

        item.setContract(contract);
    }

    private void setCustomContract(CatalogueItemDetailsDto item, AssetDraftDto draft) {
        Assert.notNull(item, "Expected a non-null item");
        Assert.notNull(draft, "Expected a non-null draft");
        Assert.isTrue(item.getContractTemplateType() == EnumContractType.UPLOADED_CONTRACT, "Expected contract type to be UPLOADED_CONTRACT");

        final List<AssetContractAnnexDto> annexes  = draft.getCommand().getContractAnnexes();
        final CustomContractDto           contract = new CustomContractDto(annexes);

        // Add template contract file information (available only for drafts)
        try {
            final Path path = this.draftFileManager.resolveContractPath(draft.getPublisher().getKey(), draft.getKey());
            if (path.toFile().exists()) {
                final File file = path.toFile();
                contract.setFileName(file.getName());
                contract.setFileSize(file.length());
            }
        } catch (final FileSystemException e) {
            // no action required if no contract exists
        }

        item.setContract(contract);
    }

    private void setContractTermsAndConditions(ProviderTemplateContractHistoryEntity template, TemplateContractDto contract) {
        template.getSections().stream()
            .filter(s -> s.getOption() != null)
            .map(s -> Pair.<Integer, MasterSectionHistoryEntity>of(s.getOption(), s.getMasterSection()))
            .map(p -> p.getRight().getOptions().get(p.getLeft()))
            .filter(s -> s.getIcon() != null)
            .map(s -> {
                final Path path = Paths.get(iconFolder, s.getIcon().getFile());
                try (final InputStream fileStream = resourceLoader.getResource(path.toString()).getInputStream()) {
                    final byte[] data = IOUtils.toByteArray(fileStream);
                    return ContractTermDto.of(s.getIcon(), s.getIcon().getCategory(), data, s.getShortDescription());
                } catch (final IOException ex) {
                    logger.warn(String.format("Failed to load resource [icon=%s, path=%s]", s.getIcon(), path), ex);
                }
                return ContractTermDto.of(s.getIcon(), s.getIcon().getCategory(), null, s.getShortDescription());
            })
            .forEach(contract.getTerms()::add);
    }

}

