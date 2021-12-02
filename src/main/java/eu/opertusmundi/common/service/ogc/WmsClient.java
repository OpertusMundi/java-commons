package eu.opertusmundi.common.service.ogc;

import java.net.URL;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.catalogue.client.WmsLayerSample;

public interface WmsClient {

    default ServiceResourceDto getMetadata(URL url, String layerName) throws OgcServiceClientException {
        return this.getMetadata(url, layerName, null, null);
    }

    /**
     * Get metadata for a WMS layer
     *
     * @param url
     * @param layerName
     * @param userName
     * @param password
     * @return
     * @throws OgcServiceClientException
     */
    ServiceResourceDto getMetadata(URL url, String layerName, String userName, String password) throws OgcServiceClientException;

    default List<WmsLayerSample> getSamples(URL url, String layerName, List<Geometry> boundaries) throws OgcServiceClientException {
        return this.getSamples(url, layerName, boundaries, null, null);
    }

    /**
     * Get sample images for a set of bounding boxes
     *
     * @param url
     * @param layerName
     * @param boundaries
     * @param userName
     * @param password
     * @return
     * @throws OgcServiceClientException
     */
    List<WmsLayerSample> getSamples(URL url, String layerName, List<Geometry> boundaries, String userName, String password) throws OgcServiceClientException;

    /**
     * Get map
     *
     * @param url
     * @param layerName
     * @param bbox
     * @param width
     * @param height
     * @return
     * @throws OgcServiceClientException
     */
    byte[] getMap(URL url, String layerName, String bbox, int width, int height) throws OgcServiceClientException;

}
