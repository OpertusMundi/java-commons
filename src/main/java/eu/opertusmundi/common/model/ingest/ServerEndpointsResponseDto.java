package eu.opertusmundi.common.model.ingest;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerEndpointsResponseDto {

    @JsonProperty("WMS")
    private String wms;

    @JsonProperty("WFS")
    private String wfs;

}
