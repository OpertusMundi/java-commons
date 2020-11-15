package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.dto.CustomerRepresentativeCommandDto;
import eu.opertusmundi.common.model.dto.CustomerRepresentativeDto;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class CustomerRrepresentativeEmbeddable {

    @Embedded
    private AddressEmbeddable address;

    @NotNull
    @Column
    private ZonedDateTime birthdate;

    @NotNull
    @Column
    private String countryOfResidence;

    @NotNull
    @Column
    private String nationality;

    @NotNull
    @Column
    private String email;

    @NotNull
    @Column
    private String firstName;

    @NotNull
    @Column
    private String lastName;

    @Override
    public CustomerRrepresentativeEmbeddable clone() {
        final CustomerRrepresentativeEmbeddable r = new CustomerRrepresentativeEmbeddable();

        r.address            = this.address.clone();
        r.birthdate          = this.birthdate;
        r.countryOfResidence = this.countryOfResidence;
        r.email              = this.email;
        r.firstName          = this.firstName;
        r.lastName           = this.lastName;
        r.nationality        = this.nationality;

        return r;
    }

    public CustomerRepresentativeDto toDto() {
        final CustomerRepresentativeDto r = new CustomerRepresentativeDto();

        r.setAddress(this.address.toDto());
        r.setBirthdate(this.birthdate);
        r.setCountryOfResidence(this.countryOfResidence);
        r.setEmail(this.email);
        r.setFirstName(this.firstName);
        r.setLastName(this.lastName);
        r.setNationality(this.nationality);

        return r;
    }

    public static CustomerRrepresentativeEmbeddable from(CustomerRepresentativeCommandDto c) {
        final CustomerRrepresentativeEmbeddable r = new CustomerRrepresentativeEmbeddable();

        r.birthdate          = c.getBirthdate();
        r.countryOfResidence = c.getCountryOfResidence();
        r.email              = c.getEmail();
        r.firstName          = c.getFirstName();
        r.lastName           = c.getLastName();
        r.nationality        = c.getNationality();

        if (c.getAddress() != null) {
            r.address = AddressEmbeddable.from(c.getAddress());
        }

        return r;
    }
}
