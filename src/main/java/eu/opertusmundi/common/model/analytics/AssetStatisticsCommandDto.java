package eu.opertusmundi.common.model.analytics;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import eu.opertusmundi.common.domain.CountryEntity;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AssetStatisticsCommandDto {
	
    private String pid;

    private EnumTopicCategory segment;
    
    private ZonedDateTime publicationDate;

    private BigDecimal maxPrice;
    
    private List<CountryEntity> countries;
}
