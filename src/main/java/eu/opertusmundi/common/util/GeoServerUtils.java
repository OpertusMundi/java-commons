package eu.opertusmundi.common.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import org.geotools.ows.wms.WMSCapabilities;
import org.geotools.ows.wms.WMSUtils;
import org.geotools.ows.wms.WebMapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;

@Service
public class GeoServerUtils {

    private static final Logger logger = LoggerFactory.getLogger(GeoServerUtils.class);

    @Value("${opertusmundi.geoserver.endpoint:}")
    private String geoServerEndpoint;

    public ServiceResourceDto getCapabilities(EnumSpatialDataServiceType type, String serviceEndpoint, String layerName) {
        try {
            final String endpoint = this.appendRelativePath(this.geoServerEndpoint, serviceEndpoint);
            final URL    url      = new URL(endpoint);

            switch (type) {
                case WMS :
                    final WebMapServer wms = new WebMapServer(url);

                    final WMSCapabilities capabilities = wms.getCapabilities();

                    return Arrays.asList(WMSUtils.getNamedLayers(capabilities)).stream()
                        .filter(l -> l.getName().equals(layerName))
                        .map(l -> {
                            return ServiceResourceDto.builder()
                                .serviceType(EnumSpatialDataServiceType.WMS)
                                .build();
                        })
                        .findFirst()
                        .orElse(null);


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
