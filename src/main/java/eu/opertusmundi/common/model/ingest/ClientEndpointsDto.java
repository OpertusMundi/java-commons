package eu.opertusmundi.common.model.ingest;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class ClientEndpointsDto {

    private ClientEndpointsDto(JsonNode node) {
        this.wms = node.at("/WMS").asText();
        this.wfs = node.at("/WFS").asText();
    }

    private String wms;

    private String wfs;

    public static ClientEndpointsDto fromJsonNode(JsonNode node) {
        return new ClientEndpointsDto(node);
    }

}
