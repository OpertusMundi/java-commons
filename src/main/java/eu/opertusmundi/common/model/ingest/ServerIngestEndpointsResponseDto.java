package eu.opertusmundi.common.model.ingest;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServerIngestEndpointsResponseDto {

    @JsonProperty("WMS")
    private String wms;

    @JsonProperty("WFS")
    private String wfs;

}
