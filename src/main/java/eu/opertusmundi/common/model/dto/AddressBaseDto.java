package eu.opertusmundi.common.model.dto;

import javax.validation.Valid;

import org.hibernate.validator.constraints.Length;

import lombok.Getter;
import lombok.Setter;

@Valid
public class AddressBaseDto {

    @Length(max = 120)
    @Getter
    @Setter
    String streetName;

    @Length(max = 10)
    @Getter
    @Setter
    String streetNumber;

    @Length(max = 120)
    @Getter
    @Setter
    String city;

    @Length(max = 80)
    @Getter
    @Setter
    String region;

    @Length(max = 40)
    @Getter
    @Setter
    String country;

    @Length(max = 10)
    @Getter
    @Setter
    String postalCode;

    @Length(max = 10)
    @Getter
    @Setter
    String floorApartment;

}
