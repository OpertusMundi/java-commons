package eu.opertusmundi.common.model.keycloak.server;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Getter
@lombok.ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CredentialDto
{
    public static final String PASSWORD_TYPE = "password";
    
    public static final String HOTP_TYPE = "hotp"; /* HMAC-based OTP */
    
    public static final String TOTP_TYPE = "totp"; /* Time-based OTP */
    
    public static final String SECRET_TYPE = "secret";
    
    CredentialDto() {}
    
    public static CredentialDto ofPassword(String password)
    {
        return ofPassword(password, false);
    }
    
    public static CredentialDto ofPassword(String password, boolean temporary)
    {
        Assert.hasText(password, "password must not be empty");
        
        CredentialDto cred = new CredentialDto();
        cred.type = PASSWORD_TYPE;
        cred.value = password;
        cred.temporary = Boolean.valueOf(temporary);
        return cred;
    }
    
    public static CredentialDto ofSecret(String secret)
    {
        Assert.hasText(secret, "secret must not be empty");
        
        CredentialDto cred = new CredentialDto();
        cred.type = SECRET_TYPE;
        cred.value = secret;
        return cred;
    }
    
    @JsonProperty("type")
    private String type = PASSWORD_TYPE;
    
    @lombok.Setter
    @JsonProperty("temporary")
    private Boolean temporary;
    
    @JsonProperty("value")
    private String value;
}
