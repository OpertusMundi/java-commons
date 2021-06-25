package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import eu.opertusmundi.common.model.spatial.EpsgDto;
import lombok.Getter;

@Entity(name = "Epsg")
@Table(schema = "spatial", name = "`epsg`")
public class EpsgEntity {

    @Id
    @Column(name = "`code`", updatable = false)
    @Getter
    private Integer code;

    @Column(name = "`name`")
    @Getter
    private String name;

    @Column(name = "`active`")
    @Getter
    private boolean active;

    public EpsgDto toDto() {
        final EpsgDto e = new EpsgDto();
        e.setActive(active);
        e.setCode(code);
        e.setName(name);
        return e;
    }

}
