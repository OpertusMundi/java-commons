package eu.opertusmundi.common.model.account;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerProfessionalDto extends CustomerDto implements Serializable {

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
    private BigDecimal                pendingPayoutFunds;
    private ZonedDateTime             pendingPayoutFundsUpdatedOn;
    private String                    phone;
    private Double                    rating;
    private CustomerRepresentativeDto representative;
    private int                       saleLeadCount;
    private String                    siteUrl;

    @JsonIgnore
    private Integer pidServiceUserId;

}
