package eu.opertusmundi.common.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.analytics.AssetTotalValueQuery;
import eu.opertusmundi.common.model.analytics.AssetViewQuery;
import eu.opertusmundi.common.model.analytics.BigDecimalDataPoint;
import eu.opertusmundi.common.model.analytics.CoverageQuery;
import eu.opertusmundi.common.model.analytics.DataPoint;
import eu.opertusmundi.common.model.analytics.DataSeries;
import eu.opertusmundi.common.model.analytics.EnumTemporalUnit;
import eu.opertusmundi.common.model.analytics.SalesQuery;
import eu.opertusmundi.common.model.analytics.SegmentDimension;
import eu.opertusmundi.common.model.analytics.SpatialDimension;
import eu.opertusmundi.common.model.analytics.TemporalDimension;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import eu.opertusmundi.common.model.spatial.CountryCapitalCityDto;
import eu.opertusmundi.common.repository.AssetStatisticsRepository;
import eu.opertusmundi.common.repository.CountryRepository;
import eu.opertusmundi.common.util.StreamUtils;

@Service
public class DefaultDataAnalysisService implements DataAnalysisService, InitializingBean {

    private final Map<String, CountryCapitalCityDto> countries = new HashMap<>();

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private AssetStatisticsRepository assetStatisticsRepository;

    @Autowired(required = false)
    private ElasticSearchService elasticSearchService;

    @Override
    public void afterPropertiesSet() {
        countryRepository.getCountryCapitalCities().stream().forEach(c -> this.countries.put(c.getCode(), c.toDto()));

        // Update ISO codes with the corresponding NUTS codes
        if (countries.containsKey("GB")) {
            countries.get("GB").setCode("UK");
            countries.put("UK", countries.get("GB"));
        }
        if (countries.containsKey("GR")) {
            countries.get("GR").setCode("EL");
            countries.put("EL", countries.get("GR"));
        }
    }

    @Override
    public DataSeries<?> execute(SalesQuery query) {
        if (query == null || query.getMetric() == null) {
            return DataSeries.<BigInteger>empty();
        }

        final DataSeries<BigDecimal> result        = new DataSeries<>();
        final List<String>           groupByFields = new ArrayList<>();
        final TemporalDimension      time          = query.getTime();
        final SpatialDimension       spatial       = query.getAreas();
        final SegmentDimension       segments      = query.getSegments();
        final List<UUID>             publishers    = query.getPublishers();
        final List<String>           assets        = query.getAssets();
        final List<String>           filters       = new ArrayList<>();
        final List<Object>           args          = new ArrayList<>();

        if (time != null) {
            // Apply temporal dimension grouping
            if (time.getUnit() != null) {
                result.setTimeUnit(time.getUnit());

                if (time.getUnit().ordinal() >= EnumTemporalUnit.YEAR.ordinal()) {
                    groupByFields.add("payin_year");
                }
                if (time.getUnit().ordinal() >= EnumTemporalUnit.MONTH.ordinal()) {
                    groupByFields.add("payin_month");
                }
                if (time.getUnit().ordinal() >= EnumTemporalUnit.WEEK.ordinal()) {
                    groupByFields.add("payin_week");
                }
                if (time.getUnit().ordinal() >= EnumTemporalUnit.DAY.ordinal()) {
                    groupByFields.add("payin_day");
                }
            }

            // Apply temporal dimension filtering
            if (time.getMin() != null) {
                filters.add("payin_executed_on >= ?");
                args.add(time.getMin().atTime(0, 0, 0).atZone(ZoneId.of("UTC")));
            }
            if (time.getMax() != null) {
                filters.add("payin_executed_on <= ?");
                args.add(time.getMax().atTime(0, 0, 0).atZone(ZoneId.of("UTC")));
            }
        }

        if (spatial != null) {
            // Apply spatial grouping
            if (spatial.isEnabled()) {
                groupByFields.add("payin_country");
            }
            // Apply spatial filtering
            if (spatial.getCodes() != null && !spatial.getCodes().isEmpty()) {
                filters.add("payin_country in (" + StringUtils.repeat("?", ", ", spatial.getCodes().size()) + ")");
                args.addAll(spatial.getCodes());
            }
        }

        if (segments != null) {
            // Apply segment grouping
            if (segments.isEnabled()) {
                groupByFields.add("segment");
            }
            // Apply segment filtering
            if (segments.getSegments() != null && !segments.getSegments().isEmpty()) {
                filters.add("segment in (" + StringUtils.repeat("?", ", ", segments.getSegments().size()) + ")");
                segments.getSegments().stream().map(Object::toString).forEach(args::add);
            }
        }

        if (publishers != null) {
            // Apply publisher grouping
            if (publishers.size() > 1) {
                groupByFields.add("CAST(provider_key as char varying)");
            }

            // Apply publisher filtering
            if (!publishers.isEmpty()) {
                filters.add("provider_key in (" + StringUtils.repeat("CAST(? as uuid)", ", ", publishers.size()) + ")");
                args.addAll(publishers);
            }
        }

        if (assets != null) {
            // Apply asset grouping
            if (assets.size() > 1) {
                groupByFields.add("asset_pid");
            }

            // Apply asset filtering
            if (!assets.isEmpty()) {
                filters.add("asset_pid in (" + StringUtils.repeat("?", ", ", assets.size()) + ")");
                args.addAll(assets);
            }
        }

        String sqlString = groupByFields.isEmpty() ? "select count(*), sum(payin_total_price) " : "select %1$s , count(*), sum(payin_total_price) ";

        sqlString += "from \"analytics\".payin_item_hist ";

        if (filters.size() > 0) {
            sqlString += "where      " + String.join(" and ", filters) + " ";
        }

        if(!groupByFields.isEmpty()) {
            sqlString +=
                "group by   " + String.join(", ", groupByFields) + " "
              + "order by   " + String.join(", ", groupByFields);
        }

        if (!groupByFields.isEmpty()) {
            sqlString = String.format(sqlString, String.join(", ", groupByFields));
        }

        final Query nativeQuery = entityManager.createNativeQuery(sqlString);

        for (int index = 0; index < args.size(); index++) {
            nativeQuery.setParameter(index + 1, args.get(index));
        }

        @SuppressWarnings("unchecked")
        final List<Object[]> rows = nativeQuery.getResultList();

        for (final Object[] r : rows) {
            result.getPoints().add(this.mapObjectToDataPoint(query, r));
        }

        // Update coordinates
        for (final DataPoint<BigDecimal> p : result.getPoints()) {
            if (p.getLocation() != null) {
                final CountryCapitalCityDto country = countries.get(p.getLocation().getCode());
                if (country != null) {
                    p.getLocation().setLat(country.getLatitude());
                    p.getLocation().setLon(country.getLongitude());
                }
            }
        }

        return result;
    }


    @Override
    public DataSeries<?> execute(AssetViewQuery query) {
        if (elasticSearchService == null || query == null || query.getMetric() == null) {
            return DataSeries.<BigInteger>empty();
        }

        final DataSeries<BigDecimal> result = elasticSearchService.searchAssetViews(query);

        // Update coordinates
        for (final DataPoint<BigDecimal> p : result.getPoints()) {
            if (p.getLocation() != null) {
                final CountryCapitalCityDto country = countries.get(p.getLocation().getCode());
                if (country != null) {
                    p.getLocation().setLat(country.getLatitude());
                    p.getLocation().setLon(country.getLongitude());
                }
            }
        }

        return result;
    }

    private DataPoint<BigDecimal> mapObjectToDataPoint(SalesQuery query, Object[] o) {
        final TemporalDimension time       = query.getTime();
        final SpatialDimension  spatial    = query.getAreas();
        final SegmentDimension  segments   = query.getSegments();
        final List<UUID>        publishers = query.getPublishers();
        final List<String>      assets     = query.getAssets();

        final DataPoint<BigDecimal> p     = new DataPoint<>();
        int                         index = 0;

        // The order we extract values must match the order we apply grouping
        // fields
        if (time != null) {
            if (time.getUnit() != null) {
                p.setTime(new DataPoint.TimeInstant());

                if (time.getUnit().ordinal() >= EnumTemporalUnit.YEAR.ordinal()) {
                    p.getTime().setYear((Integer) o[index++]);
                }
                if (time.getUnit().ordinal() >= EnumTemporalUnit.MONTH.ordinal()) {
                    p.getTime().setMonth((Integer) o[index++]);
                }
                if (time.getUnit().ordinal() >= EnumTemporalUnit.WEEK.ordinal()) {
                    p.getTime().setWeek((Integer) o[index++]);
                }
                if (time.getUnit().ordinal() >= EnumTemporalUnit.DAY.ordinal()) {
                    p.getTime().setDay((Integer) o[index++]);
                }
            }
        }

        if (spatial != null && spatial.isEnabled()) {
            p.setLocation(DataPoint.Location.of((String) o[index++]));
        }

        if (segments != null && segments.isEnabled()) {
            p.setSegment((String) o[index++]);
        }

        if (publishers != null && publishers.size() > 1) {
            p.setPublisher(UUID.fromString((String) o[index++]));
        }

        if (assets != null && assets.size() > 1) {
            p.setAsset((String) o[index++]);
        }

        switch (query.getMetric()) {
            case COUNT_TRANSACTIONS :
                p.setValue(BigDecimal.valueOf(((BigInteger) o[index]).longValue()));
                break;
            case SUM_SALES :
                p.setValue(((BigDecimal) o[++index]).setScale(2, RoundingMode.HALF_UP));
                break;
        }

        return p;
    }

    @Override
    public DataSeries<?> executeCoverage(CoverageQuery query) {
        if (query == null) {
            return DataSeries.<BigInteger>empty();
        }

        final DataSeries<BigDecimal>  result   = new DataSeries<>();
        final TemporalDimension       time     = query.getTime();
        final List<EnumTopicCategory> segments = query.getSegments();
        final List<String>            filters  = new ArrayList<>();
        final List<Object>            args     = new ArrayList<>();

        // Apply filtering
        if (time != null) {
            if (time.getMin() != null) {
                filters.add("publication_date >= ?");
                args.add(time.getMin().atTime(0, 0, 0).atZone(ZoneId.of("UTC")));
            }
            if (time.getMax() != null) {
                filters.add("publication_date <= ?");
                args.add(time.getMax().atTime(0, 0, 0).atZone(ZoneId.of("UTC")));
            }
        }
        if (segments != null && !segments.isEmpty()) {
            filters.add("segment in (" + StringUtils.repeat("?", ", ", segments.size()) + ")");
            segments.stream().map(Object::toString).forEach(args::add);
        }

        String sqlString = "select country_code , count(*) ";

        sqlString += "from \"analytics\".asset_statistics as s ";
        sqlString += "inner join \"analytics\".asset_statistics_country as c ";
        sqlString += "on s.id = c.statistic ";

        if (filters.size() > 0) {
            sqlString += "where      " + String.join(" and ", filters) + " ";
        }

        sqlString += "group by c.country_code;";

        final Query nativeQuery = entityManager.createNativeQuery(sqlString);

        for (int index = 0; index < args.size(); index++) {
            nativeQuery.setParameter(index + 1, args.get(index));
        }

        @SuppressWarnings("unchecked")
        final List<Object[]> rows = nativeQuery.getResultList();

        for (final Object[] r : rows) {
            result.getPoints().add(this.mapObjectToDataPoint(r));
        }

        // Update coordinates
        for (final DataPoint<BigDecimal> p : result.getPoints()) {
            if (p.getLocation() != null) {
                final CountryCapitalCityDto country = countries.get(p.getLocation().getCode());
                if (country != null) {
                    p.getLocation().setLat(country.getLatitude());
                    p.getLocation().setLon(country.getLongitude());
                }
            }
        }

        return result;
    }

    private DataPoint<BigDecimal> mapObjectToDataPoint(Object[] o) {

        final DataPoint<BigDecimal> p = new DataPoint<>();

        p.setLocation(DataPoint.Location.of((String) o[0]));
        p.setValue(BigDecimal.valueOf(((BigInteger) o[1]).longValue()));

        return p;
    }

    @Override
    public DataSeries<?> executeTotalAssetValue(AssetTotalValueQuery query) {
        if (query == null) {
            return DataSeries.<BigInteger>empty();
        }

        final DataSeries<BigDecimal> result          = new DataSeries<>();
        final TemporalDimension      time            = query.getTime();
        List<BigDecimalDataPoint>    assetStatistics = new ArrayList<>();

        if (time != null && time.getUnit() != null) {
            // Apply temporal dimension grouping
            result.setTimeUnit(time.getUnit());

            switch (time.getUnit()) {
                case YEAR :
                    assetStatistics = this.assetStatisticsRepository.findTotalFileAssetValuePerYear();
                    break;
                case MONTH :
                    assetStatistics = this.assetStatisticsRepository.findTotalFileAssetValuePerMonth();
                    break;
                case WEEK :
                    assetStatistics = this.assetStatisticsRepository.findTotalFileAssetValuePerWeek();
                    break;
                case DAY :
                    assetStatistics = this.assetStatisticsRepository.findTotalFileAssetValuePerDay();
                    break;
            }
            StreamUtils.from(assetStatistics).forEach(result.getPoints()::add);
        } else {
            final BigDecimal            value = this.assetStatisticsRepository.findTotalFileAssetValue().orElse(BigDecimal.ZERO);
            final DataPoint<BigDecimal> p     = new DataPoint<>();

            p.setValue(value);

            result.getPoints().add(p);
        }

        return result;
    }

    @Override
    public List<ImmutablePair<String, Integer>> executePopularAssetViewsAndSearches(AssetViewQuery query) {
        if (elasticSearchService == null || query == null) {
            return Collections.emptyList();
        }

        return this.elasticSearchService.findPopularAssetViewsAndSearches(query);
    }

    @Override
    public List<ImmutablePair<String, Integer>> executePopularTerms() {
        if (elasticSearchService == null) {
            return Collections.emptyList();
        }

        return this.elasticSearchService.findPopularTerms();
    }

}
