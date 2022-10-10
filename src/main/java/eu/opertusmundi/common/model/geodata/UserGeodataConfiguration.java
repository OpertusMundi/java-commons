package eu.opertusmundi.common.model.geodata;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Geodata information for a specific user
 */
@AllArgsConstructor(staticName = "of")
@Getter
public class UserGeodataConfiguration {

    /**
     * The absolute URL of the geodata shard
     */
    private final String url;

    /**
     * The geodata shard unique identifier e.g. `s1`
     */
    private final String shard;

    /**
     * User GeoServer workspace
     *
     * <p>
     * By default the user's unique key prefixed by an underscore
     */
    private final String workspace;

    /**
     * WMS service endpoint
     */
    private final String wmsEndpoint;

    /**
     * WFS service endpoint
     */
    private final String wfsEndpoint;

    /**
     * WMTS service endpoint
     */
    private final String wmtsEndpoint;

}
