package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import eu.opertusmundi.common.model.account.BirthplaceCommandDto;
import eu.opertusmundi.common.model.account.BirthplaceDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BirthplaceEmbeddable implements Cloneable {

    @Column
    @EqualsAndHashCode.Include
    private String city;

    @Column
    @EqualsAndHashCode.Include
    private String country;

    @Override
    public BirthplaceEmbeddable clone() {
        final BirthplaceEmbeddable p = new BirthplaceEmbeddable();

        p.city    = this.city;
        p.country = this.country;

        return p;
    }

    public BirthplaceDto toDto() {
        final BirthplaceDto p = new BirthplaceDto();

        p.setCity(this.city);
        p.setCountry(this.country);

        return p;
    }

    public static BirthplaceEmbeddable from(BirthplaceCommandDto c) {
        final BirthplaceEmbeddable p = new BirthplaceEmbeddable();

        p.setCity(c.getCity());
        p.setCountry(c.getCountry());

        return p;
    }

}
