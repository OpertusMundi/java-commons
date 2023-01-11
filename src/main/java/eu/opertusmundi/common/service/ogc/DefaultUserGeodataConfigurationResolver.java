package eu.opertusmundi.common.service.ogc;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.MessageFormat;

import eu.opertusmundi.common.config.GeodataConfiguration;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.geodata.EnumGeodataWorkspace;
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

    @Value("${opertusmundi.geodata.public-workspace.prefix:_}")
    private String publicUserWorkspacePrefix;

    @Value("${opertusmundi.geodata.private-workspace.prefix:p_}")
    private String privateUserWorkspacePrefix;

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
        key = "'account-' + #key + '-' + #workspaceType"
    )
    public UserGeodataConfiguration resolveFromUserKey(UUID key, EnumGeodataWorkspace workspaceType) {
        final AccountDto account            = this.accountRepository.findOneByKeyObject(key).get();
        final UUID       userKey            = Optional.ofNullable(account.getParentKey()).orElse(account.getKey());
        final Shard      shard              = this.geodataConfiguration.getShardById(account.getProfile().getGeodataShard());
        final String     publicWorkspace    = shard == null ? this.defaultWorkspace : publicUserWorkspacePrefix + userKey.toString();
        final String     privateWorkspace   = shard == null ? this.defaultWorkspace : privateUserWorkspacePrefix + userKey.toString();
        final String     geoserverEndpoint  = shard == null ? defaultGeoServerEndpoint : shard.getEndpoint();

        final String effectiveWorkspace = switch (workspaceType) {
            case PUBLIC -> publicWorkspace;
            case PRIVATE -> privateWorkspace;
        };

        final var arguments = new HashMap<String, Object>();
        arguments.put("shard", shard == null ? "" : shard.getId());
        arguments.put("workspace", effectiveWorkspace);

        final String wmsEndpoint  = MessageFormat.format(wmsEndpointTemplate, arguments);
        final String wfsEndpoint  = MessageFormat.format(wfsEndpointTemplate, arguments);
        final String wmtsEndpoint = MessageFormat.format(wmtsEndpointTemplate, arguments);

        return UserGeodataConfiguration.of(
            workspaceType,
            geoserverEndpoint,
            shard == null ? null : shard.getId(),
            effectiveWorkspace,
            StringUtils.appendIfMissing(wmsEndpoint, "/"),
            StringUtils.appendIfMissing(wfsEndpoint, "/"),
            StringUtils.appendIfMissing(wmtsEndpoint, "/")
        );
    }
}
