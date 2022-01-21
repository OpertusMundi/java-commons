package eu.opertusmundi.common.service.integration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQuery;
import eu.opertusmundi.common.model.catalogue.integration.OpenDataSentinelHubProperties;
import eu.opertusmundi.common.model.catalogue.integration.SentinelHubProperties;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import eu.opertusmundi.common.model.pricing.QuotationDto;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.model.pricing.QuotationMessageCode;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;
import eu.opertusmundi.common.model.pricing.SystemQuotationParametersDto;
import eu.opertusmundi.common.model.pricing.integration.SHSubscriptionPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.integration.SHSubscriptionQuotationParametersDto;
import eu.opertusmundi.common.model.sinergise.SubscriptionPlanDto;
import eu.opertusmundi.common.service.CatalogueService;

@Service("sentinelHubDataProvider")
@ConditionalOnProperty(name = "opertusmundi.sentinel-hub.enabled")
public class SentinelHubDataProviderIntegrationService extends BaseDataProviderIntegrationService implements DataProviderIntegrationService {

    private final List<EnumAssetType> supportedType = Arrays.asList(
        EnumAssetType.SENTINEL_HUB_OPEN_DATA,
        EnumAssetType.SENTINEL_HUB_COMMERCIAL_DATA
    );

    private final SentinelHubService sentinelHub;

    private final CatalogueService catalogueService;

    @Autowired
    public SentinelHubDataProviderIntegrationService(
        SentinelHubService sentinelHub,
        CatalogueService catalogueService
    ) {
        this.catalogueService = catalogueService;
        this.sentinelHub      = sentinelHub;
    }

    @Override
    protected boolean supports(EnumAssetType type) {
        return supportedType.contains(type);
    }

    @Override
    protected boolean supports(EnumPricingModel model) {
        return this.supportedType.stream()
            .flatMap(t -> t.getAllowedPricingModels().stream())
            .anyMatch(m -> m == model);
    }

    @Override
    public void validateCatalogueItem(CatalogueItemCommandDto command, Errors errors) {
        if (!this.supports(command.getType())) {
            return;
        }
        if (command.getExtensions() == null) {
            return;
        }

        final SentinelHubProperties props = command.getExtensions().getSentinelHub();

        // Custom extensions are required
        if (props == null) {
            errors.rejectValue("extensions.sentinelHub", EnumValidatorError.NotNull.name());
            return;
        }

        // Get existing assets
        final ElasticAssetQuery query = ElasticAssetQuery.builder()
            .type(Arrays.asList(command.getType()))
            .build();

        final List<CatalogueItemDto> items = catalogueService.findAllAdvanced(null, query).getResult().getItems();

        switch (props.getType()) {
            case OPEN_DATA : {
                final OpenDataSentinelHubProperties openDataProps = (OpenDataSentinelHubProperties) props;

                // Parent identifier is required and cannot change after is set
                final CatalogueItemDto parent = items.stream()
                    .filter(i -> this.compareCollection(i, openDataProps.getCollection()))
                    .findFirst()
                    .orElse(null);

                if (parent != null) {
                    if (StringUtils.isBlank(command.getParentId())) {
                        // Parent identifier is required
                        errors.rejectValue("parentId", EnumValidatorError.NotEmpty.name());
                    } else if (!command.getParentId().equals(parent.getId())) {
                        // Parent identifier must match the identifier of the
                        // already published asset with the same open data
                        // collection
                        errors.rejectValue("parentId", EnumValidatorError.NotValid.name());
                    }
                } else if (!StringUtils.isBlank(command.getParentId())) {
                    errors.rejectValue("parentId", EnumValidatorError.NotValid.name());
                }

                break;
            }

            case COMMERCIAL : {
                break;
            }
        }
    }

    @Override
    public void updatePricingModels(CatalogueItemDto item) {
        if (!this.supports(item.getType())) {
            return;
        }
        if (item.getExtensions() == null) {
            return;
        }

        final List<SubscriptionPlanDto> plans = this.sentinelHub.getSubscriptionPlans();

        final List<BasePricingModelCommandDto> models = plans.stream()
            .map(p -> SHSubscriptionPricingModelCommandDto.builder()
                .annualPriceExcludingTax(p.getBilling().getAnnually())
                .monthlyPriceExcludingTax(p.getBilling().getMonthly())
                .key(UUID.fromString(p.getId()))
                .build()
            )
            .collect(Collectors.toList());

        item.setPricingModels(models);
    }

    @Override
    public QuotationDto createQuotation(
        BasePricingModelCommandDto model, QuotationParametersDto userParams, SystemQuotationParametersDto systemParams
    ) {
        if (!this.supports(model.getType())) {
            return null;
        }

        switch (model.getType()) {
            case SENTINEL_HUB_SUBSCRIPTION :
                final List<SubscriptionPlanDto> plans = this.sentinelHub.getSubscriptionPlans();
                final SubscriptionPlanDto selected = plans.stream()
                    .filter(p -> p.getId().equals(model.getKey().toString()))
                    .findFirst()
                    .orElse(null);

                if(selected == null) {
                    throw new QuotationException(
                        QuotationMessageCode.PRICING_MODEL_NOT_FOUND,
                        String.format("Pricing model [%s] not found", model.getKey())
                    );
                }

                return this.computeSubscriptionQuotation(model, userParams, systemParams);

            case SENTINEL_HUB_IMAGES :
                // TODO : Invoke Sentinel Hub API
                throw new QuotationException(QuotationMessageCode.PRICING_MODEL_NOT_IMPLEMENTED, "Not implemented");
            default :
                return null;
        }
    }

    private QuotationDto computeSubscriptionQuotation(
        BasePricingModelCommandDto model, QuotationParametersDto userParams, SystemQuotationParametersDto systemParams
    ) {
        final SHSubscriptionPricingModelCommandDto typedModel  = (SHSubscriptionPricingModelCommandDto) model;
        final SHSubscriptionQuotationParametersDto typedParams = (SHSubscriptionQuotationParametersDto) userParams;

        if (typedParams.getFrequency() != null) {
            final QuotationDto quotation = new QuotationDto();
            quotation.setTaxPercent(systemParams.getTaxPercent().intValue());

            switch (typedParams.getFrequency()) {
                case MONTHLY :
                    quotation.setTotalPriceExcludingTax(typedModel.getMonthlyPriceExcludingTax());
                    break;
                case ANNUAL :
                    quotation.setTotalPriceExcludingTax(typedModel.getAnnualPriceExcludingTax());
                    break;
                default :
                    throw new QuotationException(
                        QuotationMessageCode.SUBSCRIPTION_FREQUENCY_NOT_SUPPORTED,
                        "Frequency [%s] is not supported",
                        typedParams.getFrequency()
                    );
            }

            quotation.setTax(quotation.getTotalPriceExcludingTax()
                .multiply(systemParams.getTaxPercent())
                .divide(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP)
            );

            return quotation;
        }

        return null;
    }

    private boolean compareCollection(CatalogueItemDto item, String collection) {
        final SentinelHubProperties props = item.getExtensions().getSentinelHub();

        if (props == null || props.getType() != SentinelHubProperties.EnumType.OPEN_DATA) {
            return false;
        }

        final OpenDataSentinelHubProperties openDataProps = (OpenDataSentinelHubProperties) props;

        return openDataProps.getCollection().equalsIgnoreCase(collection);
    }

}
