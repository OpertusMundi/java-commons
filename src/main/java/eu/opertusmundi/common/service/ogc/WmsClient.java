package eu.opertusmundi.common.service.ogc;

import java.net.URL;

import eu.opertusmundi.common.model.asset.ServiceResourceDto;

public interface WmsClient {

    default ServiceResourceDto GetMetadata(URL url, String workspace, String typeName) throws OgcServiceClientException {
        return this.GetMetadata(url, workspace, typeName, null, null);
    }

    ServiceResourceDto GetMetadata(URL url, String workspace, String typeName, String userName, String password) throws OgcServiceClientException;

}
