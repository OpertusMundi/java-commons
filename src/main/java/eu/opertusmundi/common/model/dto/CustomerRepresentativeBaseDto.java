package eu.opertusmundi.common.model.dto;

import java.time.ZonedDateTime;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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

    @NotEmpty
    protected String email;

    @NotEmpty
    protected String firstName;

    @NotEmpty
    protected String lastName;

}
