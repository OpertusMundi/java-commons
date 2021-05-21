package eu.opertusmundi.common.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.analytics.BaseQuery;
import eu.opertusmundi.common.model.analytics.DataPoint;
import eu.opertusmundi.common.model.analytics.DataSeries;
import eu.opertusmundi.common.model.analytics.SalesQuery;

@Service
public class DefaultDataAnalysisService implements DataAnalysisService {

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    @Override
    public DataSeries<?> execute(SalesQuery query) {
        if (query == null || query.getMetric() == null) {
            return DataSeries.<BigInteger>empty();
        }

        final DataSeries<BigDecimal>      result        = new DataSeries<>();
        final List<String>                groupByFields = new ArrayList<>();
        final BaseQuery.TemporalDimension time          = query.getTime();
        final BaseQuery.SpatialDimension  spatial       = query.getAreas();
        final BaseQuery.SegmentDimension  segments      = query.getSegments();
        final List<UUID>                  publishers    = query.getPublishers();
        final List<String>                assets        = query.getAssets();
        final List<String>                filters       = new ArrayList<>();
        final List<Object>                args          = new ArrayList<>();

        if (time != null) {
            result.setTimeUnit(time.getUnit());

            // Apply temporal dimension grouping
            if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.YEAR.ordinal()) {
                groupByFields.add("payin_year");
            }
            if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.MONTH.ordinal()) {
                groupByFields.add("payin_month");
            }
            if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.WEEK.ordinal()) {
                groupByFields.add("payin_week");
            }
            if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.DAY.ordinal()) {
                groupByFields.add("payin_day");
            }

            // Apply temporal dimension filtering
            if (time.getMin() != null) {
                filters.add("payin_executed_on >= ?");
                args.add(time.getMin());
            }
            if (time.getMax() != null) {
                filters.add("payin_executed_on <= ?");
                args.add(time.getMax());
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

        String sqlString =
            "select     %1$s , count(*), sum(payin_total_price) "
          + "from       \"analytics\".payin_item_hist "
          + "where      " + String.join(" and ", filters) + " "
          + "group by   " + String.join(", ", groupByFields);


        sqlString = String.format(sqlString, String.join(", ", groupByFields));

        final Query nativeQuery = entityManager.createNativeQuery(sqlString);

        for (int index = 0; index < args.size(); index++) {
            nativeQuery.setParameter(index + 1, args.get(index));
        }

        @SuppressWarnings("unchecked")
        final List<Object[]> rows = nativeQuery.getResultList();

        for (final Object[] r : rows) {
            result.getPoints().add(this.mapObjectToDataPoint(query, r));
        }

        return result;
    }

    private DataPoint<BigDecimal> mapObjectToDataPoint(SalesQuery query, Object[] o) {
        final BaseQuery.TemporalDimension time       = query.getTime();
        final BaseQuery.SpatialDimension  spatial    = query.getAreas();
        final BaseQuery.SegmentDimension  segments   = query.getSegments();
        final List<UUID>                  publishers = query.getPublishers();
        final List<String>                assets     = query.getAssets();

        final DataPoint<BigDecimal> p     = new DataPoint<>();
        int                         index = 0;

        // The order we extract values must match the order we apply grouping
        // fields
        if (time != null) {
            p.setTime(new DataPoint.TimeInstant());

            if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.YEAR.ordinal()) {
                p.getTime().setYear((Integer) o[index++]);
            }
            if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.MONTH.ordinal()) {
                p.getTime().setMonth((Integer) o[index++]);
            }
            if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.WEEK.ordinal()) {
                p.getTime().setWeek((Integer) o[index++]);
            }
            if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.DAY.ordinal()) {
                p.getTime().setDay((Integer) o[index++]);
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

}
