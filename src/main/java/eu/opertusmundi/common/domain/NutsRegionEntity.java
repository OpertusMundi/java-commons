package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.locationtech.jts.geom.Geometry;

import eu.opertusmundi.common.model.spatial.NutsRegionFeatureDto;
import eu.opertusmundi.common.model.spatial.NutsRegionPropertiesDto;
import lombok.Getter;

@Entity(name = "NutsRegion")
@Table(schema = "spatial", name = "`nuts`")
public class NutsRegionEntity {

    @Id
    @Column(name = "`gid`", updatable = false)
    @Getter
    private Integer id;

    @Column(name = "lvl_code")
    @Getter
    private Long level;

    @Column(name = "nuts_id")
    @Getter
    private String code;

    @Column(name = "name_latin")
    @Getter
    private String nameLatin;
    
    @Column(name = "nuts_name")
    @Getter
    private String name;

    @Column(name = "country")
    @Getter
    private String country;

    @Column(name = "population")
    @Getter
    private Long population;

    @Column(name = "pop_year")
    @Getter
    private Long censusYear;

    @Column(name = "`geom`")
    @Getter
    private Geometry geometry;
    
    @Column(name = "`geom_simple`")
    @Getter
    private Geometry simplifiedGeometry;

    public NutsRegionPropertiesDto toProperties() {
        final NutsRegionPropertiesDto p = new NutsRegionPropertiesDto();
        p.setCode(code);
        p.setLevel(level);
        p.setName(name);
        p.setNameLatin(nameLatin);
        p.setPopulation(population);
        return p;
    }

    public NutsRegionFeatureDto toFeature() {
        final NutsRegionFeatureDto r = new NutsRegionFeatureDto();
        r.getProperties().setCode(code);
        r.getProperties().setLevel(level);
        r.getProperties().setName(name);
        r.getProperties().setNameLatin(nameLatin);
        r.getProperties().setPopulation(population);
        r.setGeometry(simplifiedGeometry);
        return r;
    }

}
