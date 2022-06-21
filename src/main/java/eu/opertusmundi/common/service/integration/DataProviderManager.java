package eu.opertusmundi.common.service.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Conventions;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.QuotationDto;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;
import eu.opertusmundi.common.model.pricing.SystemQuotationParametersDto;

@Service
public class DataProviderManager implements DataProviderIntegrationService {

    private Map<String, DataProviderIntegrationService> dataProviders = new HashMap<>();

    @Autowired
    private ApplicationContext ctx;

    public void refreshProviders() {
        this.dataProviders = ctx.getBeansOfType(DataProviderIntegrationService.class);

        // Remove self
        this.dataProviders.remove(Conventions.getVariableName(this));
    }

    @Override
    public void validateCatalogueItem(CatalogueItemCommandDto command, Errors errors) {
        this.dataProviders.values().stream().forEach(p -> p.validateCatalogueItem(command, errors));
    }

    @Override
    public void updatePricingModels(CatalogueItemDto item) {
        this.dataProviders.values().stream().forEach(p -> p.updatePricingModels(item));
    }

    @Override
    public @Nullable QuotationDto createQuotation(
        BasePricingModelCommandDto model, QuotationParametersDto userParams, SystemQuotationParametersDto systemParams
    ) {
        final List<QuotationDto> quotations = this.dataProviders.values().stream()
            .map(p -> p.createQuotation(model, userParams, systemParams))
            .filter(q -> q != null)
            .collect(Collectors.toList());

        Assert.isTrue(quotations.size() < 2, "More than one data providers returned a quotation result. Check configuration options");

        return quotations.stream().findFirst().orElse(null);
    }

    @Override
    public void registerAsset(UUID payInKey) {
        this.dataProviders.values().stream().forEach(p -> p.registerAsset(payInKey));
    }
}
