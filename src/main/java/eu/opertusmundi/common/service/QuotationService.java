package eu.opertusmundi.common.service;

import java.util.List;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.payment.ServiceUseStatsDto;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.QuotationDto;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;

public interface QuotationService {

    /**
     * Validates the specified pricing model
     *
     * @param model
     * @param params
     * @throws QuotationException
     */
    void validate(BasePricingModelCommandDto model) throws QuotationException;

    /**
     * Validates parameters for the specified pricing model
     *
     * @param model
     * @param params
     * @param ignoreMissing
     * @throws QuotationException
     */
    void validate(BasePricingModelCommandDto model, QuotationParametersDto params, boolean ignoreMissing) throws QuotationException;

    /**
     * Computes a quotation
     *
     * Ignores command's asset PID parameter and computes quotation based on the
     * specified asset
     * @param asset
     * @param pricingModelKey
     * @param userParams
     * @param ignoreMissing If true, any missing parameters are ignored; Otherwise an exception is thrown
     * @return
     * @throws QuotationException
     */
    EffectivePricingModelDto createQuotation(
        CatalogueItemDto asset, String pricingModelKey, QuotationParametersDto userParams, boolean ignoreMissing
    ) throws QuotationException;

    /**
     * Computes quotations for all asset pricing models
     *
     * For a pricing model that requires parameters, an empty effect pricing model is returned
     *
     * @param asset
     * @param command
     * @return
     * @throws QuotationException
     */
    List<EffectivePricingModelDto> createQuotation(CatalogueItemDto asset) throws QuotationException;

    /**
     * Compute a quotation for the specified service pricing model and use
     * statistics
     *
     * @param stats
     * @return
     * @throws QuotationException if the model does not support services or computation fails
     */
    QuotationDto createQuotation(BasePricingModelCommandDto model, ServiceUseStatsDto stats) throws QuotationException;

}
