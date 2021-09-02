package eu.opertusmundi.common.service.ogc;

import java.net.URL;

import eu.opertusmundi.common.model.asset.ServiceResourceDto;

public interface WmsClient {

    default ServiceResourceDto getMetadata(URL url, String layerName) throws OgcServiceClientException {
        return this.getMetadata(url, layerName, null, null);
    }

    ServiceResourceDto getMetadata(URL url, String layerName, String userName, String password) throws OgcServiceClientException;

}
