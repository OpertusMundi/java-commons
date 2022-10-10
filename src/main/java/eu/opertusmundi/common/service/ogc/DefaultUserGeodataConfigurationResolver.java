package eu.opertusmundi.common.service.ogc;

import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.MessageFormat;

import eu.opertusmundi.common.config.GeodataConfiguration;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.geodata.Shard;
import eu.opertusmundi.common.model.geodata.UserGeodataConfiguration;
import eu.opertusmundi.common.repository.AccountRepository;

/**
 * Utility service for resolving user geodata configuration
 */
@Service
public class DefaultUserGeodataConfigurationResolver implements UserGeodataConfigurationResolver {

    @Value("${opertusmundi.geoserver.endpoint:}")
    private String defaultGeoServerEndpoint;

    @Value("${opertusmundi.geoserver.workspace:opertusmundi}")
    private String defaultWorkspace;

    @Value("${opertusmundi.geodata.workspace.prefix:_}")
    private String userWorkspacePrefix;

    @Value("${opertusmundi.services.wms.endpoint:}")
    private String wmsEndpointTemplate;

    @Value("${opertusmundi.services.wfs.endpoint:}")
    private String wfsEndpointTemplate;

    @Value("${opertusmundi.services.wmts.endpoint:}")
    private String wmtsEndpointTemplate;

    private final GeodataConfiguration geodataConfiguration;
    private final AccountRepository    accountRepository;

    @Autowired
    public DefaultUserGeodataConfigurationResolver(GeodataConfiguration geodataConfiguration, AccountRepository accountRepository) {
        this.geodataConfiguration = geodataConfiguration;
        this.accountRepository    = accountRepository;
    }

    @Override
    @Cacheable(
        cacheNames = "geodata-configuration",
        cacheManager = "defaultCacheManager",
        key = "'account-' + #key"
    )
    public UserGeodataConfiguration resolveFromUserKey(UUID key) {
        final AccountDto account           = this.accountRepository.findOneByKeyObject(key).get();
        final Shard      shard             = this.geodataConfiguration.getShardById(account.getProfile().getGeodataShard());
        final String     workspace         = shard == null ? this.defaultWorkspace : userWorkspacePrefix + key.toString();
        final String     geoserverEndpoint = shard == null ? defaultGeoServerEndpoint : shard.getEndpoint();

        final var arguments = new HashMap<String, Object>();
        arguments.put("shard", shard == null ? "" : shard.getId());
        arguments.put("workspace", workspace);

        final String wmsEndpoint  = MessageFormat.format(wmsEndpointTemplate, arguments);
        final String wfsEndpoint  = MessageFormat.format(wfsEndpointTemplate, arguments);
        final String wmtsEndpoint = MessageFormat.format(wmtsEndpointTemplate, arguments);

        return UserGeodataConfiguration.of(
            geoserverEndpoint,
            shard == null ? null : shard.getId(),
            workspace,
            StringUtils.appendIfMissing(wmsEndpoint, "/"),
            StringUtils.appendIfMissing(wfsEndpoint, "/"),
            StringUtils.appendIfMissing(wmtsEndpoint, "/")
        );
    }
}
