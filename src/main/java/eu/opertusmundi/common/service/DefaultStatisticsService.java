package eu.opertusmundi.common.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.domain.CountryEuropeEntity;
import eu.opertusmundi.common.model.analytics.AssetStatisticsCommandDto;
import eu.opertusmundi.common.model.analytics.AssetStatisticsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.QuotationDto;
import eu.opertusmundi.common.model.spatial.CountryEuropeDto;
import eu.opertusmundi.common.repository.AssetStatisticsRepository;
import eu.opertusmundi.common.repository.CountryRepository;
import eu.opertusmundi.common.util.StreamUtils;

@Service
public class DefaultStatisticsService implements StatisticsService{

    @Autowired
    private AssetStatisticsRepository assetStatisticsRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Override
    public AssetStatisticsDto updateStatisticsPublishAsset(CatalogueItemDto item) {
    	final AssetStatisticsCommandDto assetStatisticsCommandDto = new AssetStatisticsCommandDto();

        final Geometry                       geom            = item.getGeometry();
        final EnumTopicCategory              segment         = StreamUtils.from(item.getTopicCategory()).findFirst().orElse(null);
        final ZonedDateTime                  publicationDate = LocalDateTime.parse(item.getPublicationDate()).atZone(ZoneId.of("UTC"));

        BigDecimal maxPrice = new BigDecimal(0);
        for (final EffectivePricingModelDto p : item.getEffectivePricingModels()) {
            final QuotationDto quotation = p.getQuotation();
            if (quotation == null) {
                continue;
            }
            if (quotation.getTotalPriceExcludingTax().compareTo(maxPrice) == 1) {
                maxPrice = quotation.getTotalPriceExcludingTax();
            }
        }

        // Set geometry SRID before executing the query
        if (geom != null && geom.getSRID() == 0) {
            geom.setSRID(4326);
        }
        final List<CountryEuropeDto> countries = this.countryRepository.getCountriesWithinGeometry(geom).stream()
            .map(CountryEuropeEntity::toDto)
            .collect(Collectors.toList());

        assetStatisticsCommandDto.setPid(item.getId());
        assetStatisticsCommandDto.setSegment(segment);
        assetStatisticsCommandDto.setPublicationDate(publicationDate);
        assetStatisticsCommandDto.setMaxPrice(maxPrice);
        assetStatisticsCommandDto.setCountries(countries);

    	return this.assetStatisticsRepository.create(assetStatisticsCommandDto);
    }

    @Override
    public void updateStatisticsUnpublishAsset(String pid) {
    	this.assetStatisticsRepository.setStatisticInactive(pid);
    }

}
