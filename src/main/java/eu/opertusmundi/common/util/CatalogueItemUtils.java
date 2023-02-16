package eu.opertusmundi.common.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.AssetContractAnnexEntity;
import eu.opertusmundi.common.domain.MasterSectionHistoryEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractHistoryEntity;
import eu.opertusmundi.common.model.account.EnumKycLevel;
import eu.opertusmundi.common.model.account.ProviderDto;
import eu.opertusmundi.common.model.asset.AssetContractAnnexDto;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.contract.ContractSectionOptionDto;
import eu.opertusmundi.common.model.contract.ContractTermDto;
import eu.opertusmundi.common.model.contract.CustomContractDto;
import eu.opertusmundi.common.model.contract.TemplateContractDto;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.AssetContractAnnexRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractHistoryRepository;
import eu.opertusmundi.common.service.DraftFileManager;
import eu.opertusmundi.common.service.QuotationService;
import eu.opertusmundi.common.service.integration.DataProviderManager;

/**
 * Utility service that provides methods to set computed properties of a
 * marketplace catalogue item
 */
@Service
public class CatalogueItemUtils {

    private static final Logger logger = LoggerFactory.getLogger(CatalogueItemUtils.class);

    @Value("${opertusmundi.contract.icons}")
    private String iconFolder;

    private final AccountRepository                         accountRepository;
    private final DataProviderManager                       dataProviderManager;
    private final QuotationService                          quotationService;
    private final ResourceLoader                            resourceLoader;
    private final AssetContractAnnexRepository              assetContractAnnexRepository;
    private final ProviderTemplateContractHistoryRepository providerContractRepository;
    private final DraftFileManager                          draftFileManager;

    @Autowired
    public CatalogueItemUtils(
        AccountRepository accountRepository,
        AssetContractAnnexRepository assetContractAnnexRepository,
        DataProviderManager dataProviderManager,
        DraftFileManager draftFileManager,
        ProviderTemplateContractHistoryRepository providerContractRepository,
        QuotationService quotationService,
        ResourceLoader resourceLoader
    ) {
        this.accountRepository            = accountRepository;
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

    /**
     * Set the contract from a published asset
     *
     * @param item
     * @param feature
     */
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

    /**
     * Set the contract from an asset draft
     *
     * @param item
     * @param draft
     */
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
            .map(s -> Triple.<Integer, List<Integer>, MasterSectionHistoryEntity>of(s.getOption(), s.getSubOption(), s.getMasterSection()))
            .map(t -> Pair.<List<Integer>, ContractSectionOptionDto>of(t.getMiddle(), t.getRight().getOptions().get(t.getLeft())))
            .map(t -> {
                final var section = t.getRight();
                final var icon = section.getIcon();
                final var description = section.getShortDescription();
                final var text = new ArrayList<String>();
                text.add(description);
                if (CollectionUtils.isNotEmpty(section.getSubOptions())) {
                    StreamUtils.from(t.getLeft())
                        .sorted()
                        .map(index -> section.getSubOptions().get(index).getBodyHtml())
                        .filter(value -> !StringUtils.isBlank(value))
                        .forEach(text::add);
                }

                if (icon != null) {
                    final var category = icon.getCategory();
                    final var path     = Paths.get(iconFolder, icon.getFile());
                    try (final InputStream fileStream = resourceLoader.getResource(path.toString()).getInputStream()) {
                        final byte[] data = IOUtils.toByteArray(fileStream);
                        return ContractTermDto.of(icon, category, data, description, text);
                    } catch (final IOException ex) {
                        logger.warn(String.format("Failed to load resource [icon=%s, path=%s]", icon, path), ex);
                    }
                    return ContractTermDto.of(icon, category, null, description, text);
                }
                return ContractTermDto.of(null, null, null, description, text);
            })
            .filter(t -> !StringUtils.isBlank(t.getDescription()) && CollectionUtils.isNotEmpty(t.getText()) && t.getIcon() != null)
            .forEach(contract.getTerms()::add);
    }

    /**
     * Set publisher information to a catalogue item
     *
     * @param item
     */
    public void setPublisher(CatalogueItemDetailsDto item) {
        final AccountEntity account   = this.accountRepository.findOneByKey(item.getPublisherId()).orElse(null);
        final ProviderDto   publisher = account == null ? null : account.getProvider().toProviderDto(true);

        item.setPublisher(publisher);
        item.setAvailableToPurchase(publisher != null && publisher.getKycLevel() == EnumKycLevel.REGULAR);
    }

}

