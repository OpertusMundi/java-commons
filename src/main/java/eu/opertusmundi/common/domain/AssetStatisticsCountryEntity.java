package eu.opertusmundi.common.domain;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AssetStatisticsCountry")
@Table(schema = "analytics", name = "`asset_statistics_country`",
	   uniqueConstraints = {@UniqueConstraint(name = "uq_asset_statistics_country_key", columnNames = {"`statistic`", "`country_code`"})}
)
@Getter
@Setter
public class AssetStatisticsCountryEntity {
	
    @Id
    @SequenceGenerator(sequenceName = "analytics.asset_statistics_country_id_seq", name = "asset_statistics_country_id_seq", allocationSize = 1)
    @NotNull
    @Column(name = "`id`")
    private Integer id;

    @NotNull
    @ManyToOne(targetEntity=AssetStatisticsEntity.class)
    @JoinColumn(name = "`statistic`", nullable = false)
    AssetStatisticsEntity statistic;

    @NotNull
    @Column(name = "`country_code`", updatable = false)
    private String countryCode;
	
    public AssetStatisticsCountryEntity(AssetStatisticsEntity statistic, String countryCode) {
        this.statistic 		= statistic;
        this.countryCode	= countryCode;
    }

}