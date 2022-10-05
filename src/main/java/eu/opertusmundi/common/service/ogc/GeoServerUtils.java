package eu.opertusmundi.common.service.ogc;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.catalogue.client.WfsLayerSample;
import eu.opertusmundi.common.model.catalogue.client.WmsLayerSample;
import eu.opertusmundi.common.model.ingest.ResourceIngestionDataDto;

@Service
public class GeoServerUtils {

    private static final Logger logger = LoggerFactory.getLogger(GeoServerUtils.class);

    @Autowired
    private WmsClient wmsClient;

    @Autowired
    private WfsClient wfsClient;

    public ServiceResourceDto getCapabilities(
        EnumSpatialDataServiceType type,
        String geoserverEndpoint,
        String relativeEndpoint,
        String workspace,
        String layer
    ) {
        try {
            final String endpoint = this.appendRelativePath(geoserverEndpoint, relativeEndpoint);
            final URI    uri      = new URI(endpoint);

            switch (type) {
                case WMS :
                    return this.wmsClient.getMetadata(uri.toURL(), layer);
                case WFS:
                    return this.wfsClient.getMetadata(uri.toString(), workspace, layer);

                default :
                    return null;
            }
        } catch (final Exception ex) {
            logger.error(String.format("Failed to execute GetCapabilities. [url=%s]", geoserverEndpoint), ex);
        }
        return null;
    }

    public List<WmsLayerSample> getWmsSamples(String geoserverEndpoint, ResourceIngestionDataDto config, List<Geometry> boundaries) {
        final ResourceIngestionDataDto.ServiceEndpoint endpoint  = config.getEndpointByServiceType(EnumSpatialDataServiceType.WMS);
        final String                                   layerName = config.getTableName();

        try {
            final String path = this.appendRelativePath(geoserverEndpoint, endpoint.getUri());
            final URI    uri  = new URI(path);

            return this.wmsClient.getSamples(uri.toURL(), layerName, boundaries);
        } catch (final Exception ex) {
            logger.error(String.format(
                "Failed to fetch WMS sample. [url=%s, layerName=%s, boundaries=%]",
                endpoint.getUri(), layerName, boundaries
            ), ex);
        }
        return null;
    }

    public List<WfsLayerSample> getWfsSamples(String geoserverEndpoint, String workspace, ResourceIngestionDataDto config, List<Geometry> boundaries) {
        final ResourceIngestionDataDto.ServiceEndpoint endpoint  = config.getEndpointByServiceType(EnumSpatialDataServiceType.WFS);
        final String                                   layerName = config.getTableName();

        try {
            final String path = this.appendRelativePath(geoserverEndpoint, endpoint.getUri());
            final URI    uri  = new URI(path);

            final List<WfsLayerSample> result = this.wfsClient.getSamples(uri.toURL(), workspace, layerName, boundaries);

            result.stream().forEach(s -> {
                final ObjectNode n = (ObjectNode) s.getData();
                n.put("totalFeatures", config.getFeatures());
            });

            return result;
        } catch (final Exception ex) {
            logger.error(String.format(
                "Failed to fetch WFS sample. [url=%s,workspace=%s,layerName=%s, boundaries=%s]",
                endpoint.getUri(), workspace, layerName, boundaries
            ), ex);
        }
        return null;
    }

    public byte[] getWmsMap(
        String geoserverEndpoint, String relativeEndpoint, String layerName, String bbox, int width, int height
    ) throws OgcServiceClientException, MalformedURLException, URISyntaxException {
        final String endpoint = this.appendRelativePath(geoserverEndpoint, relativeEndpoint);
        final URI    uri      = new URI(endpoint);

        return this.wmsClient.getMap(uri.toURL(), layerName, bbox, width, height);
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
