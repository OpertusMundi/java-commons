package eu.opertusmundi.common.service.ogc;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;

@Service
public class GeoServerUtils {

    private static final Logger logger = LoggerFactory.getLogger(GeoServerUtils.class);

    @Value("${opertusmundi.geoserver.endpoint:}")
    private String geoServerEndpoint;

    @Value("${opertusmundi.geoserver.workspace:opertusmundi}")
    private String workspace;

    @Autowired
    private WmsClient wmsClient;

    @Autowired
    private WfsClient wfsClient;

    public ServiceResourceDto getCapabilities(EnumSpatialDataServiceType type, String serviceEndpoint, String layerName) {
        try {
            final String endpoint = this.appendRelativePath(this.geoServerEndpoint, serviceEndpoint);
            final URI    uri      = new URI(endpoint);

            switch (type) {
                case WMS :
                    return this.wmsClient.getMetadata(uri.toURL(), layerName);
                case WFS:
                    return this.wfsClient.getMetadata(uri.toString(), workspace, layerName);

                default :
                    return null;
            }
        } catch (final Exception ex) {
            logger.error(String.format("Failed to execute GetCapabilities. [url=%s]", serviceEndpoint), ex);
        }
        return null;
    }

    private String appendRelativePath(String basePath, String relativePath) throws URISyntaxException {
        final URI relativeUri = new URI(relativePath);
        if (relativeUri.isAbsolute()) {
            return relativePath;
        }

        final boolean includeSlash = !basePath.endsWith("/") && !relativePath.startsWith("/");

        return basePath + (includeSlash ? "/" : "") + relativePath;
    }

}
