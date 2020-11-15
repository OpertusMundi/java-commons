package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import eu.opertusmundi.common.model.dto.AddressCommandDto;
import eu.opertusmundi.common.model.dto.AddressDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AddressEmbeddable {

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
    public AddressEmbeddable clone() {
        final AddressEmbeddable a = new AddressEmbeddable();

        a.city       = this.city;
        a.country    = this.country;
        a.line1      = this.line1;
        a.line2      = this.line2;
        a.postalCode = this.postalCode;
        a.region     = this.region;

        return a;
    }

    public AddressDto toDto() {
        final AddressDto a = new AddressDto();

        a.setCity(this.city);
        a.setCountry(this.country);
        a.setLine1(this.line1);
        a.setLine2(this.line2);
        a.setPostalCode(this.postalCode);
        a.setRegion(this.region);

        return a;
    }

    public static AddressEmbeddable from(AddressCommandDto c) {
        final AddressEmbeddable a = new AddressEmbeddable();

        a.setCity(c.getCity());
        a.setCountry(c.getCountry());
        a.setLine1(c.getLine1());
        a.setLine2(c.getLine2());
        a.setPostalCode(c.getPostalCode());
        a.setRegion(c.getRegion());

        return a;
    }

}
