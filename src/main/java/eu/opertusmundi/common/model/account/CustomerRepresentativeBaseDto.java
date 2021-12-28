package eu.opertusmundi.common.model.account;

import java.time.ZonedDateTime;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRepresentativeBaseDto {

    @NotNull
    protected ZonedDateTime birthdate;

    @NotEmpty
    protected String countryOfResidence;

    @NotEmpty
    protected String nationality;

    @Email
    @Size(min = 0, max = 255)
    protected String email;

    @NotEmpty
    @Size(min = 1, max = 100)
    protected String firstName;

    @NotEmpty
    @Size(min = 1, max = 100)
    protected String lastName;

}
