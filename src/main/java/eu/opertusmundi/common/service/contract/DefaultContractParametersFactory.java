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

		final ContractParametersDto.Provider 		provider 		= new ContractParametersDto.Provider("Adaptas", "736 Jim Rosa Lane, San Francisco, CA 94108", "richardmsteffen@armyspy.com", "Richard M. Steffen", "012345678", "098765432");
		final ContractParametersDto.Consumer 		consumer 		= new ContractParametersDto.Consumer("Life's Gold", "51, rue Adolphe Wurtz, 97420 LE PORT", "paulmstamper@teleworm.us", "Paul M. Stamper", "012345678", "632769332");
		final ContractParametersDto.Product  		product 		= new ContractParametersDto.Product("bdb87e25-4ac9-4a1e-85be-df4dced3d286", "Lakes of Greece", "Vector dataset with complete collection of the lakes in Greece", "Yes", "Yes", "Immediate", "csv file, digital download", "0%");

		final EnumPricingModel 		pricingModelType 				= EnumPricingModel.FIXED_PER_ROWS;
		final String				pricingModelDescription 		= "The product includes only the current version, with no access to updates. ";
        final EnumContinent[]		consumerRestrictionContinents	= {EnumContinent.EUROPE, EnumContinent.ASIA};
        final String[]				consumerRestrictionCountries	= {"Greece"};
        final EnumContinent[]		coverageRestrictionContinents	= {EnumContinent.AFRICA};
        final String[]				coverageRestrictionCountries	= {"Germany"};
        final String[]				domainRestrictions				= {"Domain1", "Domain2"};
        final String				totalPrice						= "100";
        final String				totalPriceExcludingTax			= "77";
        final String				pricePerRows					= "10";
        final String				pricePerPopulation				= "12";
        final List<DiscountRateDto>	discountRates					= new ArrayList<DiscountRateDto>();
        final DiscountRateDto dis1 = new DiscountRateDto();
        dis1.setCount((long) 1000);
        dis1.setDiscount(BigDecimal.valueOf(10));
        discountRates.add(dis1);
        final DiscountRateDto dis2 = new DiscountRateDto();
        dis2.setCount((long) 9000);
        dis2.setDiscount(BigDecimal.valueOf(15));
        discountRates.add(dis2);
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
