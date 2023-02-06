package eu.opertusmundi.common.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.payment.ServiceUseStatsDto;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.FixedPopulationQuotationParametersDto;
import eu.opertusmundi.common.model.pricing.FixedRowPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.FixedRowQuotationParametersDto;
import eu.opertusmundi.common.model.pricing.QuotationDto;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.model.pricing.QuotationMessageCode;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;
import eu.opertusmundi.common.model.pricing.SystemQuotationParametersDto;
import eu.opertusmundi.common.repository.NutsRegionRepository;
import eu.opertusmundi.common.service.integration.DataProviderManager;
import io.jsonwebtoken.lang.Assert;

@Service
public class DefaultQuotationService implements QuotationService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultQuotationService.class);

    // TODO: Set from configuration
    private final BigDecimal tax = new BigDecimal(24);

    private final DataProviderManager  dataProviderManager;
    private final NutsRegionRepository regionRepository;
    private final TableRowCountService tableRowCountService;

    @Autowired
    public DefaultQuotationService(
        DataProviderManager  dataProviderManager,
        NutsRegionRepository regionRepository,
        TableRowCountService tableRowCountService
    ) {
        this.dataProviderManager  = dataProviderManager;
        this.regionRepository     = regionRepository;
        this.tableRowCountService = tableRowCountService;
    }

    @Override
    public void validate(BasePricingModelCommandDto model) throws QuotationException {
        model.validate();
    }

    @Override
    public void validate(BasePricingModelCommandDto model, QuotationParametersDto params, boolean ignoreMissing) throws QuotationException {
        model.validate(params, ignoreMissing);
    }

    @Override
    public EffectivePricingModelDto createQuotation(
        CatalogueItemDto asset, String pricingModelKey, QuotationParametersDto userParams, boolean ignoreMissing
    ) throws QuotationException {
        try {
            final BasePricingModelCommandDto model = asset.getPricingModels().stream()
                .filter(m -> m.getKey().equals(pricingModelKey))
                .findFirst()
                .orElse(null);

            if(model == null) {
                throw new QuotationException(
                    QuotationMessageCode.PRICING_MODEL_NOT_FOUND,
                    String.format("Pricing model [%s] not found", pricingModelKey)
                );
            }

            // Get system parameters
            final SystemQuotationParametersDto systemParams = this.getSystemParameters(asset, model, userParams);

            // Validate parameters
            this.validate(model, userParams, ignoreMissing);

            // Check if an external data provider can create a quotation
            final QuotationDto quotation = this.dataProviderManager.createQuotation(model, userParams, systemParams);
            if (quotation != null) {
                return EffectivePricingModelDto.from(model, userParams, systemParams, quotation);
            }

            return model.compute(userParams, systemParams);
        } catch(final QuotationException ex) {
            throw ex;
        } catch(final Exception ex) {
            logger.error("Quotation failed", ex);

            throw new QuotationException("Quotation failed", ex);
        }
    }

    @Override
    public List<EffectivePricingModelDto> createQuotation(CatalogueItemDto asset) throws QuotationException {
        return asset.getPricingModels().stream()
            .map(m -> {
                final SystemQuotationParametersDto systemParams = this.getSystemParameters(asset, m, null);

                // Compute default quotations without parameters. No
                // parameter validation is required. Some pricing model may
                // return an empty result
                return m.compute((QuotationParametersDto) null, systemParams);
            })
            .collect(Collectors.toList());
    }

    @Override
    public QuotationDto createQuotation(
        CatalogueItemDto asset, BasePricingModelCommandDto model, ServiceUseStatsDto stats
    ) throws QuotationException {
        if (!model.getType().isUseStatsSupported()) {
            throw new QuotationException(
                QuotationMessageCode.QUOTATION_NOT_SUPPORTED,
                String.format("Pricing model [%s] does not support service statistics parameters", model.getType())
            );
        }

        // Get system parameters
        final SystemQuotationParametersDto systemParams = this.getSystemParameters(asset, model, null);

        final QuotationDto result = model.compute(stats, systemParams);

        return result;
    }

    /**
     * Injects dynamic parameters to a quotation command object e.g. row count
     * or population
     *
     * @param model
     * @param params
     * @return
     */
    private SystemQuotationParametersDto getSystemParameters(
        CatalogueItemDto asset, BasePricingModelCommandDto model, @Nullable QuotationParametersDto userParams
    ) {
        switch (model.getType()) {
            case FIXED_PER_ROWS :
                return this.getRowCount(asset, model, userParams);

            case FIXED_FOR_POPULATION :
                return this.getPopulation(asset, model, userParams);

            default :
                // No operation
                return SystemQuotationParametersDto.of(tax);
        }
    }

    private SystemQuotationParametersDto getRowCount(
        CatalogueItemDto asset, BasePricingModelCommandDto model, @Nullable QuotationParametersDto params
    ) {
        if (params == null) {
            return null;
        }

        Assert.isInstanceOf(FixedRowPricingModelCommandDto.class, model);
        Assert.isInstanceOf(FixedRowQuotationParametersDto.class, params);

        final FixedRowPricingModelCommandDto typedModel  = (FixedRowPricingModelCommandDto) model;
        final FixedRowQuotationParametersDto typedParams = (FixedRowQuotationParametersDto) params;
        if (ArrayUtils.isNotEmpty(typedParams.getNuts()) && asset instanceof final CatalogueItemDetailsDto assetDetails) {
            final var count          = this.tableRowCountService.countRows(assetDetails, typedParams.getNuts());
            final var totalRows      = asset.getAutomatedMetadata().get(0).get("featureCount").asLong();
            final var effectiveCount = count > typedModel.getMinRows() ? count : typedModel.getMinRows();
            return SystemQuotationParametersDto.ofRows(tax, effectiveCount, totalRows);
        }

        return null;
    }

    private SystemQuotationParametersDto getPopulation(
        CatalogueItemDto asset, BasePricingModelCommandDto model, @Nullable QuotationParametersDto params
    ) {
        if (params == null) {
            return null;
        }

        Assert.isInstanceOf(FixedPopulationQuotationParametersDto.class, params);

        final var geometry = asset.getGeometry();
        if (geometry == null) {
            return null;
        }

        final FixedPopulationQuotationParametersDto typedParams = (FixedPopulationQuotationParametersDto) params;
        if (ArrayUtils.isNotEmpty(typedParams.getNuts())) {
            final var regions             = regionRepository.findByCode(typedParams.getNuts());
            long      selectionPopulation = 0;
            long      totalPopulation     = 0;
            for (final var r : regions) {
                final var regionArea = r.getGeometry().getArea();
                totalPopulation += r.getPopulation();

                final var intersection     = geometry.intersection(r.getGeometry());
                final var intersectionArea = intersection.getArea();
                selectionPopulation += Math.round((intersectionArea / regionArea) * r.getPopulation());
            }

            final int populationPercent = (int) (100 * selectionPopulation / totalPopulation);

            return SystemQuotationParametersDto.ofPopulation(tax, selectionPopulation, populationPercent, totalPopulation);
        }
        return null;
    }

}
