package eu.opertusmundi.common.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.io.WKTWriter;
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
import eu.opertusmundi.common.repository.NutsRegionRepository;
import eu.opertusmundi.common.service.ogc.UserGeodataConfigurationResolver;

@Service
public class DefaultTableRowCountService implements TableRowCountService {

    private final static String DEFAULT_CRS = "4326";

    private final GeodataConfiguration             geodataConfiguration;
    private final NutsRegionRepository             regionRepository;
    private final UserGeodataConfigurationResolver geodataConfigurationResolver;

    private final Map<String, JdbcTemplate> jdbcTemplates = new HashMap<>();

    @Autowired
    public DefaultTableRowCountService(
        GeodataConfiguration geodataConfiguration,
        NutsRegionRepository regionRepository,
        UserGeodataConfigurationResolver geodataConfigurationResolver
    ) {
        this.geodataConfiguration         = geodataConfiguration;
        this.geodataConfigurationResolver = geodataConfigurationResolver;
        this.regionRepository             = regionRepository;
    }

    @Override
    public long countRows(CatalogueItemDetailsDto asset, String[] nutCodes) {
        // Convert nut codes to geometries in WKT format
        final WKTWriter wktWriter = new WKTWriter();
        final var       args      = regionRepository.findByCode(nutCodes).stream()
            .map(r -> wktWriter.write(r.getGeometry()))
            .toArray();

        if (ArrayUtils.isEmpty(args)) {
            return 0L;
        }

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
        final var schema           = ingestedResource.getSchema();
        final var tableName        = ingestedResource.getTableName();
        var       crs              = StringUtils.isBlank(initialResource.getCrs()) ? DEFAULT_CRS : initialResource.getCrs();
        crs = crs.startsWith("EPSG") ? crs.split(":")[1] : crs;

        // Resolve a JDBC template from publisherKey
        final var jdbcTemplate = this.resolveTemplateFromEndpoint(asset.getPublisherId());

        // Build query
        var queryTemplate = """
           select count(*) from "%1$s"."%2$s"
           where
        """;
        queryTemplate += StringUtils.repeat(" ST_Intersects(ST_Transform(ST_SetSRID(geom, %3$s), %4$s), ST_GeomFromText(?, %4$s)) ",  " or ", args.length);

        final var  query  = String.format(queryTemplate, schema, tableName, crs, DEFAULT_CRS);
        final Long result = jdbcTemplate.queryForObject(query, Long.class, args);

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
