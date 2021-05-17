package eu.opertusmundi.common.service.ogc;

import eu.opertusmundi.common.model.asset.ServiceResourceDto;

public interface WfsClient {

    default ServiceResourceDto GetMetadata(String url, String workspace, String typeName) throws OgcServiceClientException {
        return this.GetMetadata(url, workspace, typeName, null, null);
    }

    ServiceResourceDto GetMetadata(String url, String workspace, String typeName, String userName, String password) throws OgcServiceClientException;

}
