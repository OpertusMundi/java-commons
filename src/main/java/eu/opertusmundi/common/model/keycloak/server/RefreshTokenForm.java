package eu.opertusmundi.common.model.keycloak.server;

import java.util.Objects;

import feign.form.FormProperty;

@lombok.ToString
@lombok.Getter
public class RefreshTokenForm
{
    @FormProperty("client_id")
    private String clientId;
    
    @FormProperty("grant_type")
    private String grantType;
    
    @FormProperty("refresh_token")
    private String refreshToken;
    
    public static RefreshTokenForm of(String clientId, String refreshToken)
    {
        final RefreshTokenForm r = new RefreshTokenForm();
        r.clientId = Objects.requireNonNull(clientId);
        r.grantType = "refresh_token";
        r.refreshToken = Objects.requireNonNull(refreshToken);
        return r;
    }
}
