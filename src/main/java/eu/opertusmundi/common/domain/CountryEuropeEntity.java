package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.locationtech.jts.geom.Geometry;

import eu.opertusmundi.common.model.spatial.CountryEuropeDto;
import lombok.Getter;

@Entity(name = "CountryEurope")
@Table(schema = "spatial", name = "`country_eu`")
public class CountryEuropeEntity {

    @Id
    @Column(name = "`code`", updatable = false)
    @Getter
    private String code;

    @Column(name = "`name`")
    @Getter
    private String name;

    @Column(name = "`geom`")
    @Getter
    private Geometry geometry;

    public CountryEuropeDto toDto() {
        final CountryEuropeDto c = new CountryEuropeDto();
        c.setCode(code);
        c.setGeometry(geometry);
        c.setName(name);
        return c;
    }

}
