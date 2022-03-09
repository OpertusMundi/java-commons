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
        Assert.hasText(password, "password must not be empty");
        
        CredentialDto cred = new CredentialDto();
        cred.type = PASSWORD_TYPE;
        cred.value = password;
        return cred;
    }
    
    @JsonProperty("type")
    private String type = PASSWORD_TYPE;
    
    @lombok.Setter
    @JsonProperty("temporary")
    private boolean temporary = false;
    
    @JsonProperty("value")
    private String value;
}
