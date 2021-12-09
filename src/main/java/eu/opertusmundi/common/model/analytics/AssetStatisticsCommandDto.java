package eu.opertusmundi.common.model.analytics;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import eu.opertusmundi.common.model.spatial.CountryEuropeDto;
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

    private List<CountryEuropeDto> countries = new ArrayList<>();
}
