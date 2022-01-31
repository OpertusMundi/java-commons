package eu.opertusmundi.common.service.integration;

import java.util.UUID;

import javax.annotation.Nullable;

import org.springframework.validation.Errors;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.QuotationDto;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;
import eu.opertusmundi.common.model.pricing.SystemQuotationParametersDto;

public interface DataProviderIntegrationService {

    /**
     * Validate a catalogue item command
     *
     * @param command
     * @param errors
     */
    void validateCatalogueItem(CatalogueItemCommandDto command, Errors errors);

    /**
     * Set the pricing models to the catalogue item
     *
     * @param item
     */
    void updatePricingModels(CatalogueItemDto item);

    /**
     * Create quotation
     *
     * @param model
     * @param userParams
     * @param systemParams
     * @return
     * @throws QuotationException
     */
    @Nullable QuotationDto createQuotation(
        BasePricingModelCommandDto model, QuotationParametersDto userParams, SystemQuotationParametersDto systemParams
    ) throws QuotationException;

    /**
     * Register assets
     *
     * @param payInKey
     */
    void registerAsset(UUID payInKey);
}
