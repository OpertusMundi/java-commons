package eu.opertusmundi.common.service.ogc;

import eu.opertusmundi.common.model.asset.ServiceResourceDto;

public interface WfsClient {

    default ServiceResourceDto getMetadata(String url, String workspace, String typeName) throws OgcServiceClientException {
        return this.getMetadata(url, workspace, typeName, null, null);
    }

    ServiceResourceDto getMetadata(String url, String workspace, String typeName, String userName, String password) throws OgcServiceClientException;

}
