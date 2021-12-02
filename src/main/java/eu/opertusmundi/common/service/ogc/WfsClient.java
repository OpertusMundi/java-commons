package eu.opertusmundi.common.service.ogc;

import java.net.URL;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.catalogue.client.WfsLayerSample;

public interface WfsClient {

    default ServiceResourceDto getMetadata(String url, String workspace, String typeName) throws OgcServiceClientException {
        return this.getMetadata(url, workspace, typeName, null, null);
    }

    ServiceResourceDto getMetadata(String url, String workspace, String typeName, String userName, String password) throws OgcServiceClientException;

    default List<WfsLayerSample> getSamples(
        URL url, String workspace, String layerName, List<Geometry> boundaries
    ) throws OgcServiceClientException {
        return this.getSamples(url, workspace, layerName, boundaries, null, null, null);
    }

    default List<WfsLayerSample> getSamples(
        URL url, String workspace, String layerName, List<Geometry> boundaries, int count
    ) throws OgcServiceClientException {
        return this.getSamples(url, workspace, layerName, boundaries, count, null, null);
    }

    List<WfsLayerSample> getSamples(
        URL url, String workspace, String layerName, List<Geometry> boundaries, Integer count, String userName, String password
    ) throws OgcServiceClientException;

}
