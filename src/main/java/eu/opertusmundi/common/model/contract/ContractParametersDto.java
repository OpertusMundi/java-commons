package eu.opertusmundi.common.model.contract;

import java.util.List;

import com.ibm.icu.text.DecimalFormat;

import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.account.CustomerIndividualDto;
import eu.opertusmundi.common.model.account.CustomerProfessionalDto;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeatureProperties;
import eu.opertusmundi.common.model.order.HelpdeskOrderItemDto;
import eu.opertusmundi.common.model.pricing.DiscountRateDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.EnumContinent;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import eu.opertusmundi.common.model.pricing.FixedPopulationPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.FixedPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.FixedRowPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.PerCallPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.PerRowPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.PrePaidTierDto;
import eu.opertusmundi.common.model.pricing.integration.SHImagePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.integration.SHSubscriptionPricingModelCommandDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ContractParametersDto {

	@AllArgsConstructor
    @Getter
    public static class Provider {

        private Provider() {

        }

        public static Provider from(CustomerProfessionalDto c) {
            final Provider result = new Provider();

            result.companyRegistrationNumber = c.getCompanyNumber();
            result.contactEmail              = c.getEmail();
            result.contactPerson             = String.format("%s %s", c.getRepresentative().getFirstName(), c.getRepresentative().getLastName()).trim();
            result.corporateName             = c.getName();
            result.euVatNumber               = "N/A";
            result.professionalAddress       = c.getHeadquartersAddress().toString();

            return result;
        }

        private String corporateName             = "";
        private String professionalAddress       = "";
        private String contactEmail              = "";
        private String contactPerson             = "";
        private String companyRegistrationNumber = "";
        private String euVatNumber               = "";
    }

	@AllArgsConstructor
    @Getter
    public static class Consumer {

        private Consumer() {

        }

        public static Consumer from(CustomerDto c) {
            final Consumer result = new Consumer();
            switch (c.getType()) {
                case INDIVIDUAL :
                	final CustomerIndividualDto d = (CustomerIndividualDto) c;
                	result.contactEmail = d.getEmail();
                	result.corporateName = d.getFullName();
                	result.professionalAddress = d.getAddress().toString();
                    break;

                case PROFESSIONAL :
                	final CustomerProfessionalDto t = (CustomerProfessionalDto) c;
                    result.companyRegistrationNumber = t.getCompanyNumber();
                    result.contactEmail = t.getEmail();
                    result.contactPerson = String.format("%s %s", t.getRepresentative().getFirstName(), t.getRepresentative().getLastName()) .trim();
                    result.corporateName = t.getName();
                    result.euVatNumber = "N/A";
                    result.professionalAddress = t.getHeadquartersAddress().toString();
                    break;

                default :
                    throw ApplicationException.fromMessage(String.format("Customer type not supported [type=%s]", c.getType()));
            }

            return result;
        }

        private String corporateName             = "N/A";
        private String professionalAddress       = "N/A";
        private String contactEmail              = "N/A";
        private String contactPerson             = "N/A";
        private String companyRegistrationNumber = "N/A";
        private String euVatNumber               = "N/A";

    }

	@AllArgsConstructor
    @Getter
    public static class Product {

        private Product() {

        }

        public static Product from(HelpdeskOrderItemDto i, CatalogueFeature f) {
            final Product                    result     = new Product();
            final CatalogueFeatureProperties properties = f.getProperties();
            final DecimalFormat              df         = new DecimalFormat("#,##0.00");
            result.applicableFees        = df.format(i.getPricingModel().getQuotation().getTotalPrice()) + " €";
            result.description           = properties.getAbstractText();
            result.estimatedDeliveryDate = "";
            result.id                    = i.getAssetId();
            result.name                  = properties.getTitle();
            result.pastVersionIncluded   = "";
            result.updatesIncluded       = "";

            return result;
        }

        private String applicableFees;
        private String description;
        private String estimatedDeliveryDate;
        private String id;
        private String mediaAndFormatOfDelivery;
        private String name;
        private String pastVersionIncluded;
        private String updatesIncluded;

    }

	@AllArgsConstructor
    @Getter
    public static class PricingModel {

		private PricingModel() {

        }

        public static PricingModel from(EffectivePricingModelDto c) {
            final PricingModel 	result 	= new PricingModel();
            final DecimalFormat df    	= new DecimalFormat("#,##0.00");

            result.pricingModelType		= c.getModel().getType();

            if (result.pricingModelType == EnumPricingModel.FIXED) {
                final FixedPricingModelCommandDto fixedPricingModelCommandDto = (FixedPricingModelCommandDto) c.getModel();
                final Integer yearsOfUpdates	= fixedPricingModelCommandDto.getYearsOfUpdates();

                if (yearsOfUpdates == null || yearsOfUpdates == 0) {
                	result.pricingModelDescription = "The product includes only the current version, with no access to updates. ";
                } else {
                	result.pricingModelDescription = "The product includes previous versions, the current one and access to updates for "
                									 + yearsOfUpdates
                									 + " years.";
                }
            	result.pricePerRows 		= null;
            	result.pricePerPopulation	= null;
            	result.discountRates		= null;

            } else if (result.pricingModelType == EnumPricingModel.FIXED_PER_ROWS) {
                final FixedRowPricingModelCommandDto fixedRowPricingModelCommandDto = (FixedRowPricingModelCommandDto) c.getModel();
                result.pricingModelDescription = "The product includes a subset per row of the current version of the asset"
                								 + ", based on the defined area of interest"
                								 + ", with no access to updates.";
            	result.pricePerRows 		= df.format(fixedRowPricingModelCommandDto.getPrice());
            	result.pricePerPopulation	= null;
            	result.discountRates		= fixedRowPricingModelCommandDto.getDiscountRates();

            } else if (result.pricingModelType == EnumPricingModel.FIXED_FOR_POPULATION) {
                final FixedPopulationPricingModelCommandDto fixedPopulationPricingModelCommandDto = (FixedPopulationPricingModelCommandDto) c.getModel();
                result.pricingModelDescription = "The product includes a subset per population coverage of the current version of the asset"
                								 + ", based on the defined area of interest"
                								 + ", with no access to updates.";
            	result.pricePerRows 		= null;
            	result.pricePerPopulation	= df.format(fixedPopulationPricingModelCommandDto.getPrice());
            	result.discountRates		= fixedPopulationPricingModelCommandDto.getDiscountRates();
            }
            
            else if (result.pricingModelType == EnumPricingModel.PER_CALL) {
                final PerCallPricingModelCommandDto perCallPricingModelCommandDto = (PerCallPricingModelCommandDto) c.getModel();
                result.pricingModelDescription = "The product includes access to the current version of the asset"
                								 + ", based on the purchased calls"
                								 + ", with no access to updates.";
            	result.pricePerCall 		= df.format(perCallPricingModelCommandDto.getPrice());
            	result.discountRates		= perCallPricingModelCommandDto.getDiscountRates();
            	result.prepaidTiers 		= perCallPricingModelCommandDto.getPrePaidTiers();
            }
            else if (result.pricingModelType == EnumPricingModel.PER_ROW) {
                final PerRowPricingModelCommandDto perRowPricingModelCommandDto = (PerRowPricingModelCommandDto) c.getModel();
                result.pricingModelDescription = "The product includes a subset of the current version of the asset"
                								 + ", based on the purchased rows"
                								 + ", with no access to updates.";
            	result.pricePerRows 		= df.format(perRowPricingModelCommandDto.getPrice());
            	result.discountRates		= perRowPricingModelCommandDto.getDiscountRates();
            	result.prepaidTiers 		= perRowPricingModelCommandDto.getPrePaidTiers();
            }
            else if (result.pricingModelType == EnumPricingModel.SENTINEL_HUB_SUBSCRIPTION) {
                final SHSubscriptionPricingModelCommandDto sHSubscriptionPricingModelCommandDto = (SHSubscriptionPricingModelCommandDto) c.getModel();
                result.pricingModelDescription = "The product includes access to the asset"
                								 + ", based on the sentinel hub subscription model";
            	result.annualPrice			= df.format(sHSubscriptionPricingModelCommandDto.getAnnualPriceExcludingTax());
            	result.monthlyPrice			= df.format(sHSubscriptionPricingModelCommandDto.getMonthlyPriceExcludingTax());
            	result.pricePerPopulation	= null;
            }
            else if (result.pricingModelType == EnumPricingModel.SENTINEL_HUB_IMAGES) {
                final SHImagePricingModelCommandDto SHImagePricingModelCommandDto = (SHImagePricingModelCommandDto) c.getModel();
                result.pricingModelDescription = "The product includes access to the asset"
						 						+ ", based on the sentinel hub subscription model";
            }

            result.domainRestrictions				= c.getModel().getDomainRestrictions();
            result.consumerRestrictionContinents	= c.getModel().getConsumerRestrictionContinents();
            result.consumerRestrictionCountries		= c.getModel().getConsumerRestrictionCountries();
            result.coverageRestrictionContinents	= c.getModel().getCoverageRestrictionContinents();
            result.coverageRestrictionCountries		= c.getModel().getCoverageRestrictionCountries();
            result.totalPrice                    	= df.format(c.getQuotation().getTotalPrice());
            result.totalPriceExcludingTax        	= df.format(c.getQuotation().getTotalPriceExcludingTax());

            return result;
        }

        private EnumPricingModel      pricingModelType;
        private String                pricingModelDescription;
        private EnumContinent[]       consumerRestrictionContinents;
        private String[]              consumerRestrictionCountries;
        private EnumContinent[]       coverageRestrictionContinents;
        private String[]              coverageRestrictionCountries;
        private String[]              domainRestrictions;
        private String                totalPrice;
        private String                totalPriceExcludingTax;
        private String                pricePerRows;
        private String                pricePerPopulation;
        private String                pricePerCall;
        private String                annualPrice;
        private String				  monthlyPrice;
        private List<DiscountRateDto> discountRates;
        private List<PrePaidTierDto>  prepaidTiers;
    }

    private Consumer     consumer;
    private Provider     provider;
    private Product      product;
    private PricingModel pricingModel;
    private String       referenceNumber;

	public ContractParametersDto() {

	}

}
