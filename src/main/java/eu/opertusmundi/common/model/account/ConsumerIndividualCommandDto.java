package eu.opertusmundi.common.model.account;

import java.io.Serializable;
import java.time.ZonedDateTime;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import eu.opertusmundi.common.validation.IsoCountryCode;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @NotEmpty
    @Size(min = 1, max = 100)
    private String firstName;

    @NotEmpty
    @Size(min = 1, max = 100)
    private String lastName;

    @Valid
    private AddressCommandDto address;

    @Schema(description = "The user's birthdate", format = "YYYY-MM-DD")
    @NotNull
    private ZonedDateTime birthdate;

    @Schema(
        description = "The user's nationality. ISO 3166-1 alpha-2 format is expected",
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2")
    )
    @NotEmpty
    @IsoCountryCode
    private String nationality;

    @Schema(
        description = "The user's country of residence. ISO 3166-1 alpha-2 format is expected",
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2")
    )
    @NotEmpty
    @IsoCountryCode
    private String countryOfResidence;

    @Schema(description = "Consumer occupation")
    private String occupation;

}
