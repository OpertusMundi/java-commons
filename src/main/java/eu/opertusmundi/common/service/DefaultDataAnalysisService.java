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

import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.analytics.AssetCountQuery;
import eu.opertusmundi.common.model.analytics.AssetTotalValueQuery;
import eu.opertusmundi.common.model.analytics.AssetTypeEarningsQuery;
import eu.opertusmundi.common.model.analytics.AssetTypeEarningsQuery.EnumDimension;
import eu.opertusmundi.common.model.analytics.AssetViewCounterDto;
import eu.opertusmundi.common.model.analytics.AssetViewQuery;
import eu.opertusmundi.common.model.analytics.BaseQuery;
import eu.opertusmundi.common.model.analytics.BigDecimalDataPoint;
import eu.opertusmundi.common.model.analytics.CoverageQuery;
import eu.opertusmundi.common.model.analytics.DataPoint;
import eu.opertusmundi.common.model.analytics.DataSeries;
import eu.opertusmundi.common.model.analytics.EnumTemporalUnit;
import eu.opertusmundi.common.model.analytics.SalesQuery;
import eu.opertusmundi.common.model.analytics.SegmentDimension;
import eu.opertusmundi.common.model.analytics.SpatialDimension;
import eu.opertusmundi.common.model.analytics.SubscribersQuery;
import eu.opertusmundi.common.model.analytics.SubscribersQuery.EnumMetric;
import eu.opertusmundi.common.model.analytics.TemporalDimension;
import eu.opertusmundi.common.model.analytics.VendorCountQuery;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import eu.opertusmundi.common.model.spatial.CountryCapitalCityDto;
import eu.opertusmundi.common.repository.AccountRepository;
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
    private AccountRepository accountRepository;

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
    public DataSeries<?> execute(AssetTypeEarningsQuery query) {
        if (query == null || query.getMetric() == null || query.getDimension() == null) {
            return DataSeries.<BigInteger>empty();
        }

        final DataSeries<BigDecimal> result        = new DataSeries<>();
        final List<String>           groupByFields = new ArrayList<>();
        final TemporalDimension      time          = query.getTime();
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

        String fileSqlString	= null;
        String apiSqlString		= null;
        String totalSqlString 	= null;
        String finalSqlString 	= null;

        fileSqlString 	= groupByFields.isEmpty() ? "select 'ASSET' as asset_type, count(*), sum(payin_total_price) " : "select %1$s, 'ASSET' as asset_type, count(*), sum(payin_total_price) ";
        apiSqlString 	= groupByFields.isEmpty() ? "select 'API' as asset_type, count(*), sum(payin_total_price) " : "select %1$s, 'API' as asset_type, count(*), sum(payin_total_price) ";
        totalSqlString 	= groupByFields.isEmpty() ? "select 'TOTAL' as asset_type, count(*), sum(payin_total_price) " : "select %1$s, 'TOTAL' as asset_type, count(*), sum(payin_total_price) ";

        fileSqlString 	+= "from \"analytics\".payin_item_hist ";
        apiSqlString	+= "from \"analytics\".payin_item_hist ";
        totalSqlString	+= "from \"analytics\".payin_item_hist ";

        fileSqlString 	+= "where asset_type = 'ASSET' ";
        apiSqlString	+= "where asset_type = 'SUBSCRIPTION' ";

        if (filters.size() > 0) {
        	fileSqlString 	+= "and " + String.join(" and ", filters) + " ";
        	apiSqlString	+= "and " + String.join(" and ", filters) + " ";
        	totalSqlString	+= "where " + String.join(" and ", filters) + " ";
        }

        if (!groupByFields.isEmpty()) {
        	fileSqlString 	+=
               "group by   " + String.join(", ", groupByFields) + " ";
        	apiSqlString 	+=
               "group by   " + String.join(", ", groupByFields) + " ";
        	totalSqlString	+=
               "group by   " + String.join(", ", groupByFields) + " ";
        }

        if (!groupByFields.isEmpty()) {
        	fileSqlString 	= String.format(fileSqlString, String.join(", ", groupByFields));
        	apiSqlString 	= String.format(apiSqlString, String.join(", ", groupByFields));
        	totalSqlString 	= String.format(totalSqlString, String.join(", ", groupByFields));
        }

        switch (query.getDimension()) {
        case FILE_ASSET:
        	finalSqlString = fileSqlString;
        	break;
        case API:
        	finalSqlString = apiSqlString;
        	break;
        case TOTAL:
        	finalSqlString = totalSqlString;
            break;
        // All cases
        default:
          finalSqlString = "select * from ( " +  fileSqlString + " union " + apiSqlString + " union " + totalSqlString + " ) as sq ";
        }

        if (!groupByFields.isEmpty()) {
        	groupByFields.add(0, "asset_type");
        	finalSqlString 	+=  "order by   " + String.join(", ", groupByFields);
        }

        final Query nativeQuery = entityManager.createNativeQuery(finalSqlString);

        if (query.getDimension() != EnumDimension.ALL_ASSET_TYPES) {
	        for (int index = 0; index < args.size(); index++) {
	            nativeQuery.setParameter(index + 1, args.get(index));
	        }
        } else {
        	int counter = 0;
        	for (int dimCase = 0 ; dimCase < 3 ; dimCase++) {
        		 for (int index = 0; index < args.size(); index++) {
     	            nativeQuery.setParameter(counter + 1, args.get(index));
     	            counter++;
     	        }
        	}
        }

        @SuppressWarnings("unchecked")
        final List<Object[]> rows = nativeQuery.getResultList();

        for (final Object[] r : rows) {
            result.getPoints().add(this.mapObjectToDataPoint(query, r));
        }

        return result;
    }

    @Override
    public DataSeries<?> execute(SubscribersQuery query) {
        if (query == null || query.getMetric() == null) {
            return DataSeries.<BigInteger>empty();
        }

        final DataSeries<BigDecimal> result        = new DataSeries<>();
        final List<String>           groupByFields = new ArrayList<>();
        final List<String>           selectFields  = new ArrayList<>();
        final TemporalDimension      time          = query.getTime();
        final List<String>           assets        = query.getAssets();
        final List<String>           filters       = new ArrayList<>();
        final List<Object>           args          = new ArrayList<>();

        if (time != null) {
            // Apply temporal dimension grouping
            if (time.getUnit() != null) {
                result.setTimeUnit(time.getUnit());

                if (time.getUnit().ordinal() >= EnumTemporalUnit.YEAR.ordinal()) {
                	if (query.getMetric() == EnumMetric.EARNINGS) {
                		groupByFields.add("payin_year");
                		selectFields.add("payin_year as year");
                	} else {
                		groupByFields.add("date_part('year', added_on)");
                		selectFields.add("cast(date_part('year', added_on) as int) as year");
                	}
                }
                if (time.getUnit().ordinal() >= EnumTemporalUnit.MONTH.ordinal()) {
                	if (query.getMetric() == EnumMetric.EARNINGS) {
                		groupByFields.add("payin_month");
                		selectFields.add("payin_month as month");
                	} else {
	                    groupByFields.add("date_part('month', added_on)");
	                    selectFields.add("cast(date_part('month', added_on) as int) as month");
                	}
                }
                if (time.getUnit().ordinal() >= EnumTemporalUnit.WEEK.ordinal()) {
                	if (query.getMetric() == EnumMetric.EARNINGS) {
                		groupByFields.add("payin_week");
                		selectFields.add("payin_week as week");
                	} else {
	                    groupByFields.add("date_part('week', added_on)");
	                    selectFields.add("cast(date_part('week', added_on) as int) as week");
                	}
                }
                if (time.getUnit().ordinal() >= EnumTemporalUnit.DAY.ordinal()) {
                	if (query.getMetric() == EnumMetric.EARNINGS) {
                		groupByFields.add("payin_day");
                		selectFields.add("payin_day as day");
                	} else {
	                    groupByFields.add("date_part('day', added_on)");
	                    selectFields.add("cast(date_part('day', added_on) as int) as day");
                	}
                }
            }

            // Apply temporal dimension filtering
            // TODO: When number of calls is ready check if new case is needed
            if (query.getMetric() == EnumMetric.EARNINGS) {
	            if (time.getMin() != null) {
	                filters.add("payin_executed_on >= ?");
	                args.add(time.getMin().atTime(0, 0, 0).atZone(ZoneId.of("UTC")));
	            }
	            if (time.getMax() != null) {
	                filters.add("payin_executed_on <= ?");
	                args.add(time.getMax().atTime(0, 0, 0).atZone(ZoneId.of("UTC")));
	            }
            } else {
	            if (time.getMin() != null) {
	                filters.add("added_on >= ?");
	                args.add(time.getMin().atTime(0, 0, 0).atZone(ZoneId.of("UTC")));
	            }
	            if (time.getMax() != null) {
	                filters.add("added_on <= ?");
	                args.add(time.getMax().atTime(0, 0, 0).atZone(ZoneId.of("UTC")));
	            }
            }
        }

        if (assets != null) {
            // Apply asset filtering
            if (!assets.isEmpty()) {

                if (query.getMetric() == EnumMetric.EARNINGS) {
                    filters.add("asset_pid in (" + StringUtils.repeat("?", ", ", assets.size()) + ")");
                    args.addAll(assets);
                } else {
                    filters.add("asset in (" + StringUtils.repeat("?", ", ", assets.size()) + ")");
                    args.addAll(assets);
                }



            }
        }

        String sqlString = "";
        String table	 = "";

        switch (query.getMetric()) {
			case COUNT_SUBSCRIBERS:
				sqlString	+= "select count(distinct consumer) as count";
				table	  	=  "\"web\".account_subscription";
				break;
			case COUNT_CALLS:
				break;
			case EARNINGS:
				sqlString 	+= 	"select sum(payin_total_price) as price";
				table 		= 	"\"analytics\".payin_item_hist";
				filters.add("asset_type = 'SUBSCRIPTION'");
				break;
			case SUBSCRIBER_LOCATION:
				sqlString	+=	"select count(1) as count";
				table	  	=  	"\"web\".account_subscription as ac_subscription "
						  	+  	"inner join \"web\".account as account "
						  	+	"on ac_subscription.consumer = account.id "
						  	+	"inner join \"web\".customer as customer "
						  	+	"on customer.account = account.id "
						  	+	"left join \"web\".customer_individual as individual "
						  	+	"on individual.id = customer.id "
						  	+	"left join \"web\".customer_professional as professional "
						  	+	"on professional.id = customer.id";
				selectFields.add("coalesce(individual.country_of_residence, professional.headquarters_address_country) as country");
				groupByFields.add("coalesce(individual.country_of_residence, professional.headquarters_address_country)");
		        filters.add("ac_subscription.cancelled_on is null");
		        filters.add("coalesce(ac_subscription.expires_on, now()) >= now()");
				break;
			case SUBSCRIBER_SEGMENT:
				sqlString	+= "select count(1) as count";
				table	  	=  "\"web\".account_subscription";
				selectFields.add("segment");
				groupByFields.add("segment");
				break;
		}

        sqlString += groupByFields.isEmpty() ? " " : ", " + String.join(", ", selectFields) + " ";

        sqlString += "from " + table + " ";

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

    private DataPoint<BigDecimal> mapObjectToDataPoint(AssetTypeEarningsQuery query, Object[] o) {
        final TemporalDimension time       = query.getTime();

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

        // Asset type
        p.setAsset((String) o[index++]);

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

    private DataPoint<BigDecimal> mapObjectToDataPoint(SubscribersQuery query, Object[] o) {
        final TemporalDimension time       = query.getTime();

        final DataPoint<BigDecimal> p     = new DataPoint<>();
        int                         index = 0;

        switch (query.getMetric()) {
			case COUNT_SUBSCRIBERS:
				p.setValue(BigDecimal.valueOf(((BigInteger) o[index++]).longValue()));
				break;
			case COUNT_CALLS:
				p.setValue(BigDecimal.valueOf(((BigInteger) o[index++]).longValue()));
				break;
			case EARNINGS:
				p.setValue(((BigDecimal) o[index++]).setScale(2, RoundingMode.HALF_UP));
				break;
			case SUBSCRIBER_LOCATION:
				p.setValue(BigDecimal.valueOf(((BigInteger) o[index++]).longValue()));
				break;
			case SUBSCRIBER_SEGMENT:
				p.setValue(BigDecimal.valueOf(((BigInteger) o[index++]).longValue()));
				break;
		}

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

        // Group by segment in case of SUBSCRIBER_SEGMENT
        if (query.getMetric() == EnumMetric.SUBSCRIBER_SEGMENT) {
        	p.setSegment((String) o[index++]);
        } else if (query.getMetric() == EnumMetric.SUBSCRIBER_LOCATION) {
        	p.setLocation(DataPoint.Location.of((String) o[index++]));
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
	                assetStatistics = this.assetStatisticsRepository.findTotalFileAssetValuePerYear(time.getMin().toString(), time.getMax().toString());
	                break;
	            case MONTH :
	                assetStatistics = this.assetStatisticsRepository.findTotalFileAssetValuePerMonth(time.getMin().toString(), time.getMax().toString());
	                break;
	            case WEEK :
	                assetStatistics = this.assetStatisticsRepository.findTotalFileAssetValuePerWeek(time.getMin().toString(), time.getMax().toString());
	                break;
	            case DAY :
	                assetStatistics = this.assetStatisticsRepository.findTotalFileAssetValuePerDay(time.getMin().toString(), time.getMax().toString());
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
    public DataSeries<?> executeAssetCount(AssetCountQuery query) {
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
	                assetStatistics = this.assetStatisticsRepository.countAssetsPerYear(time.getMin().toString(), time.getMax().toString());
	                break;
	            case MONTH :
	                assetStatistics = this.assetStatisticsRepository.countAssetsPerMonth(time.getMin().toString(), time.getMax().toString());
	                break;
	            case WEEK :
	                assetStatistics = this.assetStatisticsRepository.countAssetsPerWeek(time.getMin().toString(), time.getMax().toString());
	                break;
	            case DAY :
	                assetStatistics = this.assetStatisticsRepository.countAssetsPerDay(time.getMin().toString(), time.getMax().toString());
	                break;
	        }
            StreamUtils.from(assetStatistics).forEach(result.getPoints()::add);
        } else {
            final BigDecimal            value = this.assetStatisticsRepository.countAssets().orElse(BigDecimal.ZERO);
            final DataPoint<BigDecimal> p     = new DataPoint<>();

            p.setValue(value);

            result.getPoints().add(p);
        }

        return result;
    }

    @Override
    public List<AssetViewCounterDto> executePopularAssetViewsAndSearches(AssetViewQuery query, int limit) {
        if (elasticSearchService == null || query == null) {
            return Collections.emptyList();
        }

        return this.elasticSearchService.findPopularAssetViewsAndSearches(query, limit);
    }

    @Override
    public List<ImmutablePair<String, Integer>> executePopularTerms(BaseQuery query) {
        if (elasticSearchService == null) {
            return Collections.emptyList();
        }

        return this.elasticSearchService.findPopularTerms(query);
    }

    @Override
    public DataSeries<?> executeVendorCount(VendorCountQuery query) {
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
	                assetStatistics = this.accountRepository.countUsersWithRolePerYear(EnumRole.ROLE_PROVIDER, time.getMin().toString(), time.getMax().toString());
	                break;
	            case MONTH :
	                assetStatistics = this.accountRepository.countUsersWithRolePerMonth(EnumRole.ROLE_PROVIDER, time.getMin().toString(), time.getMax().toString());
	                break;
	            case WEEK :
	            	// TODO: Should be replaced with countUsersWithRolePerWeek (See AccountRepository.countUsersWithRolePerWeel comment)
	            	assetStatistics = this.accountRepository.countUsersWithRolePerMonth(EnumRole.ROLE_PROVIDER, time.getMin().toString(), time.getMax().toString());
	                break;
	            case DAY :
	                assetStatistics = this.accountRepository.countUsersWithRolePerDay(EnumRole.ROLE_PROVIDER, time.getMin().toString(), time.getMax().toString());
	                break;
            }
            StreamUtils.from(assetStatistics).forEach(result.getPoints()::add);
        } else {
            final BigDecimal            value = this.accountRepository.countUsersWithRole(EnumRole.ROLE_PROVIDER).orElse(BigDecimal.ZERO);
            final DataPoint<BigDecimal> p     = new DataPoint<>();

            p.setValue(value);

            result.getPoints().add(p);
        }

        return result;
    }

}
