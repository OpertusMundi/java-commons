package eu.opertusmundi.common.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.config.GeodataConfiguration;
import eu.opertusmundi.common.model.asset.EnumResourceType;
import eu.opertusmundi.common.model.asset.FileResourceDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.geodata.EnumGeodataWorkspace;
import eu.opertusmundi.common.service.ogc.UserGeodataConfigurationResolver;

@Service
public class DefaultTableRowCountService implements TableRowCountService {

    private final static String DEFAULT_CRS = "4326";

    private final GeodataConfiguration             geodataConfiguration;
    private final UserGeodataConfigurationResolver geodataConfigurationResolver;

    private final Map<String, JdbcTemplate> jdbcTemplates = new HashMap<>();

    @Autowired
    public DefaultTableRowCountService(
        GeodataConfiguration geodataConfiguration,
        UserGeodataConfigurationResolver geodataConfigurationResolver
    ) {
        this.geodataConfiguration         = geodataConfiguration;
        this.geodataConfigurationResolver = geodataConfigurationResolver;
    }

    @Override
    public long countRows(CatalogueItemDetailsDto asset, String[] nutCodes) {
        // Get ingestion data
        final var ingestInfo = asset.getIngestionInfo();

        Assert.isTrue(ingestInfo != null && ingestInfo.size() == 1, "Expected a single ingested resource");

        final var ingestedResource = ingestInfo.get(0);
        final var initialResource  = asset.getResources().stream()
            .filter(r -> r.getType() == EnumResourceType.FILE)
            .map(r -> (FileResourceDto) r)
            .filter(r -> r.getId().equals(ingestedResource.getKey()))
            .findFirst()
            .get();
        final var nutsSchema       = this.geodataConfiguration.getNuts().getSchema();
        final var nutsTableName    = this.geodataConfiguration.getNuts().getTableName();
        final var schema           = ingestedResource.getSchema();
        final var tableName        = ingestedResource.getTableName();
        var       crs              = StringUtils.isBlank(initialResource.getCrs()) ? DEFAULT_CRS : initialResource.getCrs();
        crs = crs.startsWith("EPSG") ? crs.split(":")[1] : crs;

        // Resolve a JDBC template from publisherKey
        final var jdbcTemplate = this.resolveTemplateFromEndpoint(asset.getPublisherId());

        // Build query
        final var queryTemplate = """
           select distinct count(t.*)
           from   "%1$s"."%2$s" t
                    inner join "%3$s"."%4$s" n
                      on ST_Intersects(ST_Transform(ST_SetSRID(t.geom, %5$s), %6$s), ST_SetSRID(n.geom, %6$s))
           where  n.nuts_id in (%7$s)
        """;
        final var params        = StringUtils.repeat("?", ",", nutCodes.length);
        final var query         = String.format(queryTemplate, schema, tableName, nutsSchema, nutsTableName, crs, DEFAULT_CRS, params);

        final Long result = jdbcTemplate.queryForObject(query, Long.class, (Object[]) nutCodes);

        return result;
    }

    private JdbcTemplate resolveTemplateFromEndpoint(UUID publisherKey) {
        final var shard = this.geodataConfigurationResolver.resolveFromUserKey(publisherKey, EnumGeodataWorkspace.PUBLIC).getShard();
        if (this.jdbcTemplates.containsKey(shard)) {
            return this.jdbcTemplates.get(shard);
        }

        synchronized (this.jdbcTemplates) {
            if (this.jdbcTemplates.containsKey(shard)) {
                return this.jdbcTemplates.get(shard);
            }
            final var config     = geodataConfiguration.getShardById(shard);
            final var dataSource = DataSourceBuilder.create()
                .username(config.getPostGis().getUserName())
                .password(config.getPostGis().getPassword())
                .url(config.getPostGis().getUrl())
                .driverClassName(config.getPostGis().getDriver())
                .build();

            final var jdbcTemplate = new JdbcTemplate(dataSource);
            this.jdbcTemplates.put(shard, jdbcTemplate);
            return jdbcTemplate;
        }

    }
}
