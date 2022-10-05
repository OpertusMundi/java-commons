package eu.opertusmundi.common.service.ogc;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Value("${opertusmundi.geodata.workspace.prefix:_}")
    private String userWorkspacePrefix;

    @Value("${opertusmundi.geoserver.workspace:opertusmundi}")
    private String defaultWorkspace;

    @Value("${opertusmundi.geoserver.endpoint:}")
    private String defaultGeoServerEndpoint;

    private final GeodataConfiguration geodataConfiguration;
    private final AccountRepository    accountRepository;

    @Autowired
    public DefaultUserGeodataConfigurationResolver(GeodataConfiguration geodataConfiguration, AccountRepository accountRepository) {
        this.geodataConfiguration = geodataConfiguration;
        this.accountRepository    = accountRepository;
    }

    @Override
    public UserGeodataConfiguration resolveFromUserKey(UUID key) {
        final AccountDto account           = this.accountRepository.findOneByKeyObject(key).get();
        final Shard      shard             = this.geodataConfiguration.getShardById(account.getProfile().getGeodataShard());
        final String     workspace         = shard == null ? this.defaultWorkspace : userWorkspacePrefix + key.toString();
        final String     geoserverEndpoint = shard == null ? defaultGeoServerEndpoint : shard.getEndpoint();

        return UserGeodataConfiguration.of(geoserverEndpoint, shard == null ? null : shard.getId(), workspace);
    }
}
