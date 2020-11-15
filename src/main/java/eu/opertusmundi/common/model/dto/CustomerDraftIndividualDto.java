package eu.opertusmundi.common.model.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDraftIndividualDto extends CustomerDraftDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private AddressDto    address;
    private ZonedDateTime birthdate;
    private String        countryOfResidence;
    private String        firstName;
    private String        lastName;
    private String        nationality;
    private String        occupation;

}
