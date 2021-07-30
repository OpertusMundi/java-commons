package eu.opertusmundi.common.model.contract;

import com.ibm.icu.text.DecimalFormat;

import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.account.CustomerProfessionalDto;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeatureProperties;
import eu.opertusmundi.common.model.order.HelpdeskOrderItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ContractParametersDto {

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

    private Consumer consumer;
    private Provider provider;
    private Product  product;

    public ContractParametersDto() {

    }

}
