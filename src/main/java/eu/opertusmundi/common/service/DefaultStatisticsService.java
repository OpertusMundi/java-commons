package eu.opertusmundi.common.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.domain.CountryEuropeEntity;
import eu.opertusmundi.common.model.analytics.AssetStatisticsCommandDto;
import eu.opertusmundi.common.model.analytics.AssetStatisticsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemStatistics;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.QuotationDto;
import eu.opertusmundi.common.model.rating.AssetAverageRatingDto;
import eu.opertusmundi.common.model.spatial.CountryEuropeDto;
import eu.opertusmundi.common.repository.AssetRatingRepository;
import eu.opertusmundi.common.repository.AssetStatisticsRepository;
import eu.opertusmundi.common.repository.CountryRepository;
import eu.opertusmundi.common.util.StreamUtils;

@Service
public class DefaultStatisticsService implements StatisticsService {

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Autowired
    private AssetRatingRepository assetRatingRepository;

    @Autowired
    private AssetStatisticsRepository assetStatisticsRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Override
    public AssetStatisticsDto updateAssetPublish(CatalogueItemDto item) {
    	final AssetStatisticsCommandDto assetStatisticsCommandDto = new AssetStatisticsCommandDto();

        final Geometry          geom            = item.getGeometry();
        final EnumTopicCategory segment         = StreamUtils.from(item.getTopicCategory()).findFirst().orElse(null);
        final ZonedDateTime     now             = ZonedDateTime.now();
        final ZonedDateTime     publicationDate = ZonedDateTime.of(LocalDateTime.parse(item.getPublicationDate()), now.getZone());

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
    public void updateAssetUnpublish(String pid) {
    	this.assetStatisticsRepository.setStatisticInactive(pid);
    }

    @Override
    public List<CatalogueItemStatistics> findAll(List<String> pids) {
        final Cache cache = this.cacheManager == null ? null : this.cacheManager.getCache("asset-statistics");

        // Get identifiers that are not in cache
        final List<String> nonCachedPids = cache == null
            ? pids
            : pids.stream().filter(id -> cache.get(id) == null).collect(Collectors.toList());

        // Fetch ratings for identifiers that are not already cached.
        final List<AssetAverageRatingDto> ratings = nonCachedPids.isEmpty()
            ? Collections.emptyList()
            : this.assetRatingRepository.findAllAssetsId(nonCachedPids);

        return pids.stream()
            .map(pid -> {
                // Check PID in cache
                final ValueWrapper cachedValue = cache == null ? null : cache.get(pid);
                if (cachedValue != null) {
                    return (CatalogueItemStatistics) cachedValue.get();
                }

                // Access remote service for rating
                final AssetAverageRatingDto rating = ratings.stream().filter(r -> r.getPid().equals(pid)).findFirst().orElse(null);
                // Fetch sales/downloads statistics from repository
                final Integer[] statistics = this.assetStatisticsRepository.findAssetSalesAndDownloadsByPid(pid);

                final CatalogueItemStatistics result = CatalogueItemStatistics.builder()
                    .pid(pid)
                    .downloads(statistics == null ? 0 : statistics[0])
                    .sales(statistics == null ? 0 : statistics[1])
                    .rating(rating == null ? null : rating.getValue())
                    .build();

                // Cache result
                if (cache != null) {
                    cache.put(pid, result);
                }

                return result;
            })
            .collect(Collectors.toList());
    }

    @Override
    public void increaseSales(String pid) {
        this.assetStatisticsRepository.increaseSales(pid);
    }

}
