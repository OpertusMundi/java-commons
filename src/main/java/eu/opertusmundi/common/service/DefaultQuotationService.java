package eu.opertusmundi.common.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.model.pricing.QuotationMessageCode;
import eu.opertusmundi.common.model.pricing.QuotationParametersCommandDto;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto.SystemParameters;

@Service
public class DefaultQuotationService implements QuotationService {

    // TODO: Set from configuration
    private final BigDecimal tax = new BigDecimal(24);

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
        CatalogueItemDto asset, UUID pricingModelKey, QuotationParametersCommandDto command, boolean ignoreMissing
    ) throws QuotationException {
        try {
            final BasePricingModelCommandDto pricingModel = asset.getPricingModels().stream()
                .filter(m -> m.getKey().equals(pricingModelKey))
                .findFirst()
                .orElse(null);

            if(pricingModel == null) {
                throw new QuotationException(
                    QuotationMessageCode.PRICING_MODEL_NOT_FOUND,
                    String.format("Pricing model [%s] not found", pricingModelKey)
                );
            }
            final QuotationParametersDto params = QuotationParametersDto.from(command);

            // Inject required parameters
            this.injectParameters(pricingModel, params);

            // Validate parameters
            this.validate(pricingModel, params, ignoreMissing);

            return pricingModel.compute(params);
        } catch(final QuotationException ex) {
            throw ex;
        } catch(final Exception ex) {
            throw new QuotationException("Quotation failed", ex);
        }
    }

    @Override
    public List<EffectivePricingModelDto> createQuotation(CatalogueItemDto asset) throws QuotationException {
        return asset.getPricingModels().stream()
            .map(m -> {
                final QuotationParametersDto params = new QuotationParametersDto();

                // Inject required parameters
                this.injectParameters(m, params);

                // Compute default quotations without parameters. No
                // parameter validation is required. Some pricing model may
                // return empty result
                return m.compute(params);
            })
            .collect(Collectors.toList());
    }

    /**
     * Injects dynamic parameters to a quotation command object e.g. row count
     * or population
     *
     * @param command
     */
    private void injectParameters(BasePricingModelCommandDto command, QuotationParametersDto params) {
        params.setTaxPercent(tax);

        switch (command.getType()) {
            case FIXED_PER_ROWS :
                // TODO: Compute rows based on NUTS codes
                if (params.getNuts() != null && params.getNuts().size() > 0) {
                    params.setSystemParams(SystemParameters.fromRows(10000L));
                }
                break;

            case FIXED_FOR_POPULATION :
                // TODO: Compute population based on NUTS codes
                if (params.getNuts() != null && params.getNuts().size() > 0) {
                    params.setSystemParams(SystemParameters.fromPopulation(500000L, 20));
                }

                break;

            default :
                // No operation
                break;
        }
    }

}
