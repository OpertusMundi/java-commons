package eu.opertusmundi.common.model.keycloak;

import feign.FeignException.FeignClientException;

public class KeycloakClientException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public KeycloakClientException(String message, FeignClientException cause)
    {
        super(message, cause);
    }
}
