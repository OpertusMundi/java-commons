package eu.opertusmundi.common.model.account;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ProviderProfessionalCommandDto extends CustomerCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected ProviderProfessionalCommandDto() {
        super(EnumMangopayUserType.PROFESSIONAL);

        this.customerType = EnumCustomerType.PROVIDER;
    }

    private String additionalInfo;

    @NotNull
    @Size(min = 1, max = 255)
    private String companyNumber;

    private String companyType;

    @Valid
    @NotNull
    private AddressCommandDto headquartersAddress;

    @NotNull
    private EnumLegalPersonType legalPersonType;

    @Valid
    @NotNull
    private CustomerRepresentativeCommandDto representative;

    @Schema(description = "Consumer logo. Max allowed image size is `2Mb`")
    @Size(max = 2 * 1024 * 1024)
    private byte[] logoImage;

    private String logoImageMimeType;

    @NotEmpty
    @Size(min = 1, max = 255)
    private String name;

    private String phone;

    private String siteUrl;

    @Valid
    @NotNull
    private BankAccountCommandDto bankAccount;

}
