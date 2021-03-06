package eu.opertusmundi.common.service;

import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.model.pricing.QuotationParametersCommandDto;
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
     * @throws QuotationException
     */
    void validate(BasePricingModelCommandDto model, QuotationParametersDto params) throws QuotationException;
    
    /**
     * Computes a quotation
     * 
     * Ignores command's asset PID parameter and computes quotation based on the
     * specified asset
     * 
     * @param asset
     * @param pricingModelKey
     * @param params
     * @return
     * @throws QuotationException
     */
    EffectivePricingModelDto createQuotation(
        CatalogueItemDto asset, UUID pricingModelKey, QuotationParametersCommandDto params
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
    
}
