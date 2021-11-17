package eu.opertusmundi.common.model.sinergise.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.sinergise.ErrorDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerTokenResponseDto {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonInclude(Include.NON_NULL)
    private ErrorDto error;

    @JsonIgnore
    public boolean isSuccess() {
        return error == null;
    }
}
