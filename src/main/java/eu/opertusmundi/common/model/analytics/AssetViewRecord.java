package eu.opertusmundi.common.model.analytics;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.UUID;

import eu.opertusmundi.common.model.RequestContext;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import eu.opertusmundi.common.model.location.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class AssetViewRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum EnumSource {
        /**
         * Search operation view
         */
        SEARCH,
        /**
         * Record view
         */
        VIEW,
        /**
         * Related assets operation view
         */
        REFERENCE,
        ;
    }

    private String            country;
    private ZonedDateTime     dateTime;
    private int               day;
    private String            id;
    private Location          location;
    private int               month;
    private UUID              publisherKey;
    private String            query;
    private String            remoteIpAddress;
    private EnumTopicCategory segment;
    private EnumSource        source;
    private Integer           userId;
    private UUID              userKey;
    private String            version;
    private int               week;
    private int               year;

    public static AssetViewRecord from(RequestContext ctx, CatalogueItemDto asset, String query, EnumSource source) {
        final ZonedDateTime datetime = ZonedDateTime.now();

        final EnumTopicCategory segment = asset.getTopicCategory() != null && asset.getTopicCategory().size() > 0
            ? asset.getTopicCategory().get(0)
            : null;

        return AssetViewRecord.builder()
            .country(ctx.getAccount() == null ? null : ctx.getAccount().getCountry())
            .dateTime(datetime)
            .day(datetime.getDayOfMonth())
            .id(asset.getId())
            .location(ctx.getLocation())
            .month(datetime.getMonthValue())
            .publisherKey(asset.getPublisherId())
            .query(query)
            .remoteIpAddress(ctx.getIp())
            .segment(segment)
            .source(source)
            .userId(ctx.getAccount() == null ? null : ctx.getAccount().getId())
            .userKey(ctx.getAccount() == null ? null : ctx.getAccount().getKey())
            .version(asset.getVersion())
            .week(datetime.get(WeekFields.of(Locale.getDefault()).weekOfYear()))
            .year(datetime.getYear())
            .build();
    }

}
