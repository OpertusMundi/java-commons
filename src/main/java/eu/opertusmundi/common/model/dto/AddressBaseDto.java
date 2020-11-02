package eu.opertusmundi.common.model.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import eu.opertusmundi.common.model.EnumAddressType;
import lombok.Getter;
import lombok.Setter;

@Valid
@Getter
@Setter
public class AddressBaseDto {

    @Length(max = 120)
    protected String streetName;

    @Length(max = 10)
    protected String streetNumber;

    @Length(max = 120)
    protected String city;

    @Length(max = 80)
    protected String region;

    @Length(max = 40)
    protected String country;

    @Length(max = 10)
    protected String postalCode;

    @Length(max = 10)
    protected String floorApartment;

    @NotNull
    protected EnumAddressType type;

    protected boolean main;

}
