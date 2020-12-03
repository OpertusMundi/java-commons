package eu.opertusmundi.common.model.transform;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServerTransformDeferredResponseDto {

    @JsonProperty("endpoint")
    private String resourceEndpoint;

    @JsonProperty("status")
    private String statusEndpoint;

    private String ticket;

    @JsonDeserialize(using = EnumTransformResponse.Deserializer.class)
    private EnumTransformResponse type;

}
