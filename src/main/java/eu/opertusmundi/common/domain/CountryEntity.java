package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import eu.opertusmundi.common.model.spatial.CountryDto;
import lombok.Getter;

@Entity(name = "Country")
@Table(schema = "spatial", name = "`country`")
public class CountryEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @Getter
    private Integer id;

    @Column(name = "country_name")
    @Getter
    private String name;

    @Column(name = "iso_code")
    @Getter
    private String code;

    @Column(name = "longitude")
    @Getter
    private Double longitude;

    @Column(name = "latitude")
    @Getter
    private Double latitude;

    public CountryDto toDto() {
        final CountryDto c = new CountryDto();
        c.setCode(code);
        c.setName(name);
        c.setLongitude(longitude);
        c.setLatitude(latitude);
        return c;
    }

}
