package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import eu.opertusmundi.common.model.spatial.CountryDto;
import lombok.Getter;

@Entity(name = "Country")
@Table(schema = "spatial", name = "`country_all`")
public class CountryEntity {

    @Id
    @Column(name = "`code`")
    @Getter
    private String code;

    @Column(name = "`name`")
    @Getter
    private String name;

    public CountryDto toDto() {
        final CountryDto c = new CountryDto();
        c.setCode(code);
        c.setName(name);
        return c;
    }

}
