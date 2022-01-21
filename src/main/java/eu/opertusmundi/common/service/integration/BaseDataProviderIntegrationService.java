package eu.opertusmundi.common.service.integration;

import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;

public abstract class BaseDataProviderIntegrationService {

    /**
     * Returns true if asset {@code type} is supported
     *
     * @param type
     * @return
     */
    protected abstract boolean supports(EnumAssetType type);


    /**
     * Returns true if pricing model {@code model} is supported
     *
     * @param model
     * @return
     */
    protected abstract boolean supports(EnumPricingModel model);

}
