package eu.opertusmundi.common.service.contract;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.account.CustomerProfessionalDto;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.order.HelpdeskOrderDto;
import eu.opertusmundi.common.model.order.HelpdeskOrderItemDto;
import eu.opertusmundi.common.model.pricing.DiscountRateDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.EnumContinent;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.service.CatalogueService;

@Service
public class DefaultContractParametersFactory implements ContractParametersFactory {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    CatalogueService catalogueService;

    @Override
    public ContractParametersDto create(UUID orderKey) {
        final HelpdeskOrderDto         order        = orderRepository.findOrderObjectByKey(orderKey).get();
        final HelpdeskOrderItemDto     item         = order.getItems().get(0);
        final CustomerDto              consumer     = order.getConsumer();
        final CustomerProfessionalDto  provider     = item.getProvider();
        final CatalogueFeature         feature      = catalogueService.findOneHistoryFeature(item.getAssetId(), item.getAssetVersion());
        final EffectivePricingModelDto pricingModel = item.getPricingModel();

        final ContractParametersDto params = ContractParametersDto.builder()
    		.consumer(ContractParametersDto.Consumer.from(consumer))
    		.provider(ContractParametersDto.Provider.from(provider))
    		.product(ContractParametersDto.Product.from(item, feature))
    		.pricingModel(ContractParametersDto.PricingModel.from(pricingModel))
    		.build();


        return params;
    }

    @Override
    public ContractParametersDto createWithPlaceholderData() {

		final ContractParametersDto.Provider 		provider 		= new ContractParametersDto.Provider(
				"[Supplier name]", "[Supplier professional address]", "[Supplier contact email]", 
				"[Supplier contact person]", "[Supplier company registration number]", "[Supplier EU VAT number]");
		final ContractParametersDto.Consumer 		consumer 		= new ContractParametersDto.Consumer(
				"[Customer name]", "[Customer professional address]", "[Customer contact email]", 
				"[Customer contact person]", "[Customer company registration number]", "[Customer EU VAT number]");
		final ContractParametersDto.Product  		product 		= new ContractParametersDto.Product(
				"[Applicable fees]", "[Product description]", "[Estimated delivery date]", 
				"[Product ID]", "[Media and format of delivery]", "[Product name]", 
				 "[Past versions included]", "[Updates included]");

		final EnumPricingModel 		pricingModelType 				= EnumPricingModel.UNDEFINED;
		final String				pricingModelDescription 		= null;
        final EnumContinent[]		consumerRestrictionContinents	= null;
        final String[]				consumerRestrictionCountries	= null;
        final EnumContinent[]		coverageRestrictionContinents	= null;
        final String[]				coverageRestrictionCountries	= null;
        final String[]				domainRestrictions				= null;
        final String				totalPrice						= null;
        final String				totalPriceExcludingTax			= null;
        final String				pricePerRows					= null;
        final String				pricePerPopulation				= null;
        final List<DiscountRateDto>	discountRates					= null;
		final ContractParametersDto.PricingModel	pricingModel	= new ContractParametersDto.PricingModel(
					pricingModelType, pricingModelDescription, consumerRestrictionContinents, consumerRestrictionCountries,
					coverageRestrictionContinents, coverageRestrictionCountries, domainRestrictions,
					totalPrice, totalPriceExcludingTax, pricePerRows, pricePerPopulation, discountRates);

    	final ContractParametersDto params = ContractParametersDto.builder()
        		.consumer(consumer)
        		.provider(provider)
        		.product(product)
        		.pricingModel(pricingModel)
        		.build();

        return params;
    }

}
