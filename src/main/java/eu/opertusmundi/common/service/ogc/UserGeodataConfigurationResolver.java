package eu.opertusmundi.common.service.ogc;

import java.util.UUID;

import eu.opertusmundi.common.model.geodata.EnumGeodataWorkspace;
import eu.opertusmundi.common.model.geodata.UserGeodataConfiguration;

public interface UserGeodataConfigurationResolver {

    /**
     * Resolves geodata configuration data from the user's unique key and
     * workspace type
     *
     * @param key
     * @param workspaceType
     *
     * @return
     */
    UserGeodataConfiguration resolveFromUserKey(UUID key, EnumGeodataWorkspace workspaceType);

}
