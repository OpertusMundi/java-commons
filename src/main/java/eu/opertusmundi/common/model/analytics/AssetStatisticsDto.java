package eu.opertusmundi.common.model.analytics;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetStatisticsDto {
	
    private String pid;

    private EnumTopicCategory segment;
    
    private ZonedDateTime publicationDate;

    private Integer year;

    private Integer month;

    private Integer week;

    private Integer day;

    private BigDecimal maxPrice;
    
    private Integer downloads;
    
    private Integer sales;
    
    private Boolean active;
}
