package eu.opertusmundi.common.service.ogc;

import java.net.URI;

import org.apache.http.client.methods.RequestBuilder;
import org.springframework.http.HttpMethod;

public abstract class AbstractOgcClient {

    protected RequestBuilder getBuilder(HttpMethod method, URI uri) throws Exception {
        switch (method) {
            case POST :
                return RequestBuilder.post(uri);
            case GET :
                return RequestBuilder.get(uri);
            case DELETE :
                return RequestBuilder.delete(uri);
            default :
                throw new OgcServiceClientException(
                    OgcServiceMessageCode.HTTP_METHOD_NOT_SUPPORTED,
                    String.format("HTTP method is not supported. [method=%s]", method)
                );
        }
    }

}
