package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.asset.AssetDomainRestrictionDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AssetDomainRestriction")
@Table(schema = "`provider`", name = "`asset_domain_restriction`")
public class AssetDomainRestrictionEntity {

    @Id
    @SequenceGenerator(sequenceName = "`provider.asset_domain_restriction_id_seq`", name = "asset_domain_restriction_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "asset_domain_restriction_id_seq", strategy = GenerationType.SEQUENCE)
    @Column(name = "`id`")
    @Getter
    private Integer id;

    @NotNull
    @Column(name = "`name`")
    @Getter
    @Setter
    private String name;

    @Column(name = "`active`")
    @Getter
    @Setter
    private boolean active;

    public AssetDomainRestrictionDto toDto() {
        final AssetDomainRestrictionDto r = new AssetDomainRestrictionDto();

        r.setActive(active);
        r.setId(id);
        r.setName(name);

        return r;
    }

}
