package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import eu.opertusmundi.common.model.payment.PayInAddressCommandDto;
import eu.opertusmundi.common.model.payment.PayInAddressDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ShippingAddressEmbeddable implements Cloneable {

    @Column
    @EqualsAndHashCode.Include
    private String firstName;

    @Column
    @EqualsAndHashCode.Include
    private String lastName;

    @Column
    @EqualsAndHashCode.Include
    private String line1;

    @Column
    @EqualsAndHashCode.Include
    private String line2;

    @Column
    @EqualsAndHashCode.Include
    private String city;

    @Column
    @EqualsAndHashCode.Include
    private String region;

    @Column
    @EqualsAndHashCode.Include
    private String postalCode;

    @Column
    @EqualsAndHashCode.Include
    private String country;

    @Override
    public ShippingAddressEmbeddable clone() {
        final ShippingAddressEmbeddable a = new ShippingAddressEmbeddable();

        a.firstName = this.firstName;
        a.lastName  = this.lastName;

        a.city       = this.city;
        a.country    = this.country;
        a.line1      = this.line1;
        a.line2      = this.line2;
        a.postalCode = this.postalCode;
        a.region     = this.region;

        return a;
    }

    public PayInAddressDto toDto() {
        final PayInAddressDto a = new PayInAddressDto();

        a.setFirstName(firstName);
        a.setLastName(lastName);

        a.setCity(city);
        a.setCountry(country);
        a.setLine1(line1);
        a.setLine2(line2);
        a.setPostalCode(postalCode);
        a.setRegion(region);

        return a;
    }

    public static ShippingAddressEmbeddable from(PayInAddressCommandDto c) {
        final ShippingAddressEmbeddable a = new ShippingAddressEmbeddable();

        a.setFirstName(c.getFirstName());
        a.setLastName(c.getLastName());

        a.setCity(c.getCity());
        a.setCountry(c.getCountry());
        a.setLine1(c.getLine1());
        a.setLine2(c.getLine2());
        a.setPostalCode(c.getPostalCode());
        a.setRegion(c.getRegion());

        return a;
    }

}
