package eu.opertusmundi.common.service.ogc;

import java.util.UUID;

import eu.opertusmundi.common.model.geodata.UserGeodataConfiguration;

public interface UserGeodataConfigurationResolver {

    /**
     * Resolves geodata configuration data from the user's unique key
     *
     * @param key
     * @return
     */
    UserGeodataConfiguration resolveFromUserKey(UUID key);

}
