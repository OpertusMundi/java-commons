package eu.opertusmundi.common.model.account;

import java.io.Serializable;
import java.time.ZonedDateTime;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import eu.opertusmundi.common.validation.IsoCountryCode;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ConsumerIndividualCommandDto extends ConsumerCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected ConsumerIndividualCommandDto() {
        super(EnumMangopayUserType.INDIVIDUAL);

        this.customerType = EnumCustomerType.CONSUMER;
    }

    @Builder
    public ConsumerIndividualCommandDto(
        Integer userId,
        String email,
        String firstName,
        String lastName, 
        AddressCommandDto address, 
        ZonedDateTime birthdate,
        String nationality, 
        String countryOfResidence, 
        String occupation,
        boolean workflowInstanceRequired
        
    ) {
        super(EnumMangopayUserType.INDIVIDUAL);
        this.address                  = address;
        this.birthdate                = birthdate;
        this.countryOfResidence       = countryOfResidence;
        this.customerType             = EnumCustomerType.CONSUMER;
        this.email                    = email;
        this.firstName                = firstName;
        this.lastName                 = lastName;
        this.nationality              = nationality;
        this.occupation               = occupation;
        this.userId                   = userId;
        this.workflowInstanceRequired = workflowInstanceRequired;
    }


    @NotEmpty
    @Size(min = 1, max = 100)
    private String firstName;

    @NotEmpty
    @Size(min = 1, max = 100)
    private String lastName;

    @Valid
    private AddressCommandDto address;

    @Schema(description = "The user's birthdate", format = "YYYY-MM-DD")
    private ZonedDateTime birthdate;

    @Schema(
        description = "The user's nationality. ISO 3166-1 alpha-2 format is expected",
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2")
    )
    @IsoCountryCode
    private String nationality;

    @Schema(
        description = "The user's country of residence. ISO 3166-1 alpha-2 format is expected",
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2")
    )
    @IsoCountryCode
    private String countryOfResidence;

    @Schema(description = "Consumer occupation")
    private String occupation;

}
