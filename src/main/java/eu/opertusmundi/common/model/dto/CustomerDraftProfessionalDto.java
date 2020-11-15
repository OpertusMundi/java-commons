package eu.opertusmundi.common.model.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDraftProfessionalDto extends CustomerDraftDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String                    additionalInfo;
    private BankAccountDto            bankAccount;
    private String                    companyNumber;
    private String                    companyType;
    private AddressDto                headquartersAddress;
    private EnumLegalPersonType       legalPersonType;
    private byte[]                    logoImage;
    private String                    logoImageMimeType;
    private String                    name;
    private String                    phone;
    private CustomerRepresentativeDto representative;
    private String                    siteUrl;

}
