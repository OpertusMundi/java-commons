package eu.opertusmundi.common.model.geodata;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Geodata information for a specific user
 */
@AllArgsConstructor(staticName = "of")
@Getter
public class UserGeodataConfiguration {

    private final EnumGeodataWorkspace workspaceType;

    /**
     * The absolute URL of the geodata shard
     */
    private final String url;

    /**
     * The geodata shard unique identifier e.g. `s1`
     */
    private final String shard;

    /**
     * User effective GeoServer workspace
     */
    private final String effectiveWorkspace;

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
