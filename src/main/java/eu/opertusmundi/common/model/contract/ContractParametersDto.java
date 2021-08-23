package eu.opertusmundi.common.model.contract;

import java.util.ArrayList;
import java.util.List;

import com.ibm.icu.text.DecimalFormat;

import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.account.CustomerDto;
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
            result.euVatNumber               = "";
            result.professionalAddress       = c.getHeadquartersAddress().toString();

            return result;
        }

        private String corporateName;
        private String professionalAddress;
        private String contactEmail;
        private String contactPerson;
        private String companyRegistrationNumber;
        private String euVatNumber;
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
                    break;

                case PROFESSIONAL :
                    final CustomerProfessionalDto t = (CustomerProfessionalDto) c;

                    result.companyRegistrationNumber = t.getCompanyNumber();
                    result.contactEmail = t.getEmail();
                    result.contactPerson = String.format("%s %s", t.getRepresentative().getFirstName(), t.getRepresentative().getLastName()) .trim();
                    result.corporateName = t.getName();
                    result.euVatNumber = "";
                    result.professionalAddress = t.getHeadquartersAddress().toString();

                default :
                    throw ApplicationException.fromMessage(String.format("Customer type not supported [type=%s]", c.getType()));
            }

            return result;
        }

        private String corporateName;
        private String professionalAddress;
        private String contactEmail;
        private String contactPerson;
        private String companyRegistrationNumber;
        private String euVatNumber;

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
                result.yearsOfUpdates     = fixedPricingModelCommandDto.getYearsOfUpdates();
                result.pricePerRows       = null;
                result.pricePerPopulation = null;
                result.discountRates      = null;
            } else if (result.pricingModelType == EnumPricingModel.FIXED_PER_ROWS) {
                final FixedRowPricingModelCommandDto fixedRowPricingModelCommandDto = (FixedRowPricingModelCommandDto) c.getModel();
                result.yearsOfUpdates     = null;
                result.pricePerRows       = df.format(fixedRowPricingModelCommandDto.getPrice());
                result.pricePerPopulation = null;
                result.discountRates      = fixedRowPricingModelCommandDto.getDiscountRates();
            } else if (result.pricingModelType == EnumPricingModel.FIXED_FOR_POPULATION) {
                final FixedPopulationPricingModelCommandDto fixedPopulationPricingModelCommandDto = (FixedPopulationPricingModelCommandDto) c.getModel();
                result.yearsOfUpdates     = null;
                result.pricePerRows       = null;
                result.pricePerPopulation = df.format(fixedPopulationPricingModelCommandDto.getPrice());
                result.discountRates      = fixedPopulationPricingModelCommandDto.getDiscountRates();
            }
            result.consumerRestrictionContinents = c.getModel().getConsumerRestrictionContinents();
            result.consumerRestrictionCountries  = c.getModel().getConsumerRestrictionCountries();
            result.coverageRestrictionContinents = c.getModel().getCoverageRestrictionContinents();
            result.coverageRestrictionCountries  = c.getModel().getCoverageRestrictionCountries();
            result.domainRestrictions            = c.getModel().getDomainRestrictions();
            result.nuts                          = (ArrayList<String>) c.getParameters().getNuts();
            result.totalPrice                    = df.format(c.getQuotation().getTotalPrice());
            result.totalPriceExcludingTax        = df.format(c.getQuotation().getTotalPriceExcludingTax());

            return result;
        }

        private EnumPricingModel      pricingModelType;
        private EnumContinent[]       consumerRestrictionContinents;
        private String[]              consumerRestrictionCountries;
        private EnumContinent[]       coverageRestrictionContinents;
        private String[]              coverageRestrictionCountries;
        private String[]              domainRestrictions;
        private ArrayList<String>     nuts;
        private String                totalPrice;
        private String                totalPriceExcludingTax;
        private Integer               yearsOfUpdates;
        private String                pricePerRows;
        private String                pricePerPopulation;
        private List<DiscountRateDto> discountRates;

    }

    private Consumer     consumer;
    private Provider     provider;
    private Product      product;
    private PricingModel pricingModel;

    public ContractParametersDto() {

    }

}
