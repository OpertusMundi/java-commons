package eu.opertusmundi.common.service.ogc;

import java.net.URL;

import eu.opertusmundi.common.model.asset.ServiceResourceDto;

public interface WmsClient {

    default ServiceResourceDto GetMetadata(URL url, String layerName) throws OgcServiceClientException {
        return this.GetMetadata(url, layerName, null, null);
    }

    ServiceResourceDto GetMetadata(URL url, String layerName, String userName, String password) throws OgcServiceClientException;

}
