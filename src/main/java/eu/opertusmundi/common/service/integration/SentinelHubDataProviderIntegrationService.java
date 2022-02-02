package eu.opertusmundi.common.service.integration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInItemEntity;
import eu.opertusmundi.common.domain.PayInOrderItemEntity;
import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQuery;
import eu.opertusmundi.common.model.catalogue.integration.EnumSentinelHubAssetType;
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
import eu.opertusmundi.common.model.sinergise.server.CreateContractCommandDto;
import eu.opertusmundi.common.repository.PayInRepository;
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

    private final PayInRepository payInRepository;

    @Autowired
    public SentinelHubDataProviderIntegrationService(
        SentinelHubService sentinelHub,
        CatalogueService catalogueService,
        PayInRepository payInRepository
    ) {
        this.catalogueService = catalogueService;
        this.sentinelHub      = sentinelHub;
        this.payInRepository  = payInRepository;
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
                .key(p.getId())
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

        Assert.notNull(userParams, "Expected a non-null parameters object");
        Assert.hasText(userParams.getUserName(), "Expected a non-empty user name");

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
                final boolean isSubscribed = this.sentinelHub.contractExists(userParams.getUserName());
                if (isSubscribed) {
                    throw new QuotationException(
                        QuotationMessageCode.SUBSCRIPTION_EXISTS,
                        String.format("User [%s] has already an active subscription to Sentinel Hub service", userParams.getUserName())
                    );
                }

                final SHSubscriptionPricingModelCommandDto typedModel  = (SHSubscriptionPricingModelCommandDto) model;
                final SHSubscriptionQuotationParametersDto typedParams = (SHSubscriptionQuotationParametersDto) userParams;

                return this.computeSubscriptionQuotation(typedModel, typedParams, systemParams);

            case SENTINEL_HUB_IMAGES :
                // TODO : Invoke Sentinel Hub API
                throw new QuotationException(QuotationMessageCode.PRICING_MODEL_NOT_IMPLEMENTED, "Not implemented");
            default :
                return null;
        }
    }

    @Override
    public void registerAsset(UUID payInKey) {
        final PayInEntity   payIn    = payInRepository.findOneEntityByKey(payInKey).orElse(null);
        final AccountEntity consumer = payIn.getConsumer();

        for (final PayInItemEntity payInItem : payIn.getItems()) {
            if (payInItem instanceof PayInOrderItemEntity) {
                final PayInOrderItemEntity    orderPayInItem = (PayInOrderItemEntity) payInItem;
                final OrderEntity             order          = orderPayInItem.getOrder();
                final OrderItemEntity         orderItem      = order.getItems().get(0);
                final CatalogueItemDetailsDto asset          = this.catalogueService.findOne(null, orderItem.getAssetId(), null, false);

                if (!this.supports(asset.getType())) {
                    return;
                }

                switch (asset.getType()) {
                    case SENTINEL_HUB_OPEN_DATA :
                        final String model = orderItem.getPricingModel().getModel().getKey();

                        // We support annual subscriptions with either monthly
                        // or annual payments
                        this.createSubscriptionContract(
                            consumer.getUserName(), consumer.getFirstName(), consumer.getLastName(), payIn.getExecutedOn().plusYears(1), Integer.parseInt(model)
                        );
                        break;

                    default :
                        // No action required
                        break;
                }
            }
        }
    }

    private QuotationDto computeSubscriptionQuotation(
            SHSubscriptionPricingModelCommandDto model, SHSubscriptionQuotationParametersDto userParams, SystemQuotationParametersDto systemParams
    ) {
        if (userParams.getFrequency() != null) {
            final QuotationDto quotation = new QuotationDto();
            quotation.setTaxPercent(systemParams.getTaxPercent().intValue());

            switch (userParams.getFrequency()) {
                case MONTHLY :
                    quotation.setTotalPriceExcludingTax(model.getMonthlyPriceExcludingTax());
                    break;
                case ANNUAL :
                    quotation.setTotalPriceExcludingTax(model.getAnnualPriceExcludingTax());
                    break;
                default :
                    throw new QuotationException(
                        QuotationMessageCode.SUBSCRIPTION_FREQUENCY_NOT_SUPPORTED,
                        "Frequency [%s] is not supported",
                        userParams.getFrequency()
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

        if (props == null || props.getType() != EnumSentinelHubAssetType.OPEN_DATA) {
            return false;
        }

        final OpenDataSentinelHubProperties openDataProps = (OpenDataSentinelHubProperties) props;

        return openDataProps.getCollection().equalsIgnoreCase(collection);
    }

    private void createSubscriptionContract(
        String userName, String givenName, String familyName,
        ZonedDateTime validTo, long accountTypeId
    ) {
        final boolean registered = this.sentinelHub.contractExists(userName);

        if (registered) {
            // Skip already registered subscriptions
            return;
        }

        final CreateContractCommandDto command = CreateContractCommandDto.builder()
            .accountTypeId(accountTypeId)
            .familyName(familyName)
            .givenName(givenName)
            .userEmail(userName)
            .validTo(validTo)
            .build();

        this.sentinelHub.createContract(command);
    }

}
