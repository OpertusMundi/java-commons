package eu.opertusmundi.common.feign.client;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import eu.opertusmundi.common.model.keycloak.server.RefreshTokenForm;
import eu.opertusmundi.common.model.keycloak.server.RefreshTokenResponse;

public interface KeycloakRefreshTokenFeignClient
{
    /**
     * Refresh token and obtain a new access token.
     * 
     * @param refreshTokenForm
     * 
     * @see https://datatracker.ietf.org/doc/html/rfc6749#section-6
     * @see https://github.com/keycloak/keycloak-documentation/blob/main/server_admin/topics/sessions/offline.adoc
     */
    @PostMapping(
        path = "realms/master/protocol/openid-connect/token",
        produces = MediaType.APPLICATION_JSON_VALUE, 
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    RefreshTokenResponse refreshToken(RefreshTokenForm refreshTokenForm);
}