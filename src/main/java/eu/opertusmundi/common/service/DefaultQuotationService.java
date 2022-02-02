package eu.opertusmundi.common.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.FixedPopulationQuotationParametersDto;
import eu.opertusmundi.common.model.pricing.FixedRowQuotationParametersDto;
import eu.opertusmundi.common.model.pricing.QuotationDto;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.model.pricing.QuotationMessageCode;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;
import eu.opertusmundi.common.model.pricing.SystemQuotationParametersDto;
import eu.opertusmundi.common.service.integration.DataProviderManager;
import io.jsonwebtoken.lang.Assert;

@Service
public class DefaultQuotationService implements QuotationService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultQuotationService.class);

    // TODO: Set from configuration
    private final BigDecimal tax = new BigDecimal(24);

    private final DataProviderManager dataProviderManager;

    @Autowired
    public DefaultQuotationService(DataProviderManager dataProviderManager) {
        this.dataProviderManager = dataProviderManager;
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
            final SystemQuotationParametersDto systemParams = this.getSystemParameters(model, userParams);

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
                final SystemQuotationParametersDto systemParams = this.getSystemParameters(m, null);

                // Compute default quotations without parameters. No
                // parameter validation is required. Some pricing model may
                // return an empty result
                return m.compute(null, systemParams);
            })
            .collect(Collectors.toList());
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
        BasePricingModelCommandDto model, @Nullable QuotationParametersDto userParams
    ) {
        switch (model.getType()) {
            case FIXED_PER_ROWS :
                return this.getRowCount(model, userParams);

            case FIXED_FOR_POPULATION :
                return this.getPopulation(model, userParams);

            default :
                // No operation
                return SystemQuotationParametersDto.of(tax);
        }
    }

    private SystemQuotationParametersDto getRowCount(BasePricingModelCommandDto model, @Nullable QuotationParametersDto params) {
        // TODO: Compute rows based on NUTS codes

        if (params == null) {
            return null;
        }

        Assert.isInstanceOf(FixedRowQuotationParametersDto.class, params);

        final FixedRowQuotationParametersDto typedParams = (FixedRowQuotationParametersDto) params;
        if (typedParams.getNuts() != null && typedParams.getNuts().size() > 0) {
            return SystemQuotationParametersDto.ofRows(tax, 10000L);
        }

        return null;
    }

    private SystemQuotationParametersDto getPopulation(BasePricingModelCommandDto model, @Nullable QuotationParametersDto params) {
        // TODO: Compute population based on NUTS codes

        if (params == null) {
            return null;
        }

        Assert.isInstanceOf(FixedPopulationQuotationParametersDto.class, params);

        final FixedPopulationQuotationParametersDto typedParams = (FixedPopulationQuotationParametersDto) params;
        if (typedParams.getNuts() != null && typedParams.getNuts().size() > 0) {
            return SystemQuotationParametersDto.ofPopulation(tax, 500000L, 20);
        }
        return null;
    }

}
