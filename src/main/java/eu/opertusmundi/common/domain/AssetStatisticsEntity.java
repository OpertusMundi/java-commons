package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.analytics.AssetStatisticsDto;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;

import lombok.Getter;
import lombok.Setter;

@Entity(name = "AssetStatistics")
@Table(schema = "analytics", name = "`asset_statistics`")
@Getter
@Setter
public class AssetStatisticsEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "analytics.asset_statistics_id_seq", name = "asset_statistics_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "asset_statistics_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @Column(name = "`pid`", updatable = false)
    private String pid;

    @Column(name = "`segment`", updatable = false)
    @Enumerated(EnumType.STRING)
    private EnumTopicCategory segment;
    
    @NotNull
    @Column(name = "`publication_date`", updatable = false)
    private ZonedDateTime publicationDate;

    @NotNull
    @Column(name = "`year`", updatable = false)
    private Integer year;

    @NotNull
    @Column(name = "`month`", updatable = false)
    private Integer month;

    @NotNull
    @Column(name = "`week`", updatable = false)
    private Integer week;

    @NotNull
    @Column(name = "`day`", updatable = false)
    private Integer day;
    
    @Column(name = "`max_price`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal maxPrice;
    
    @NotNull
    @Column(name = "`downloads`")
    private Integer downloads;
    
    @NotNull
    @Column(name = "`sales`")
    private Integer sales;
    
    @NotNull
    @Column(name = "`active`")
    private Boolean active;
    
    @OneToMany(
	    targetEntity = AssetStatisticsCountryEntity.class,
	    mappedBy = "statistic",
	    fetch = FetchType.LAZY,
	    cascade = CascadeType.ALL,
	    orphanRemoval = true
    )
    private final List<AssetStatisticsCountryEntity> countries = new ArrayList<>();
    
    public AssetStatisticsDto toDto() {
        final AssetStatisticsDto a = new AssetStatisticsDto();
        a.setPid(pid);
        a.setSegment(segment);
        a.setYear(year);
        a.setMonth(month);
        a.setWeek(week);
        a.setDay(day);
        a.setMaxPrice(maxPrice);
        a.setDownloads(downloads);
        a.setSales(sales);
        a.setActive(active);
        return a;
    }

    public void addCountry(String countryCode) {
        this.countries.add(new AssetStatisticsCountryEntity(this, countryCode));
    }
}