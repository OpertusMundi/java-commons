package eu.opertusmundi.common.model.dto;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsumerProfessionalCommandDto extends CustomerCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected ConsumerProfessionalCommandDto() {
        super(EnumCustomerType.PROFESSIONAL);
    }

    private String additionalInfo;

    @NotNull
    private String companyNumber;

    private String companyType;

    @Valid
    @NotNull
    private AddressCommandDto headquartersAddress;

    @NotNull
    private EnumLegalPersonType legalPersonType;

    @Valid
    @NotNull
    private CustomerRepresentativeCommandDto legalRepresentative;

    private byte[] logoImage;
    private String logoImageMimeType;

    @NotEmpty
    private String name;

    private String phone;
    private String siteUrl;

}
