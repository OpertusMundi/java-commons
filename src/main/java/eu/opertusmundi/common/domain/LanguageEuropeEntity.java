package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import eu.opertusmundi.common.model.spatial.LanguageDto;
import lombok.Getter;

@Entity(name = "LanguageEurope")
@Table(schema = "spatial", name = "`language_eu`")
public class LanguageEuropeEntity {

    @Id
    @Column(name = "`code`", updatable = false)
    @Getter
    private String code;

    @Column(name = "`name`")
    @Getter
    private String name;

    @Column(name = "`active`")
    @Getter
    private boolean active;

    public LanguageDto toDto() {
        final LanguageDto l = new LanguageDto();
        l.setActive(active);
        l.setCode(code);
        l.setName(name);
        return l;
    }

}
