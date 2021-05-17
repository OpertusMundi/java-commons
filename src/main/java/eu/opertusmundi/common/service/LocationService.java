package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.location.Location;

public interface LocationService {

    /**
     * Get location from remote IP address
     *
     * @param ip
     * @return
     */
    Location getLocation(String ip);

}
