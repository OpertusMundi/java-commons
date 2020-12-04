package eu.opertusmundi.common.model.ingest;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServerIngestDeferredResponseDto {

    @JsonProperty("endpoints")
    private String endpointsResource;

    @JsonProperty("status")
    private String statusResource;

    private String ticket;

}
