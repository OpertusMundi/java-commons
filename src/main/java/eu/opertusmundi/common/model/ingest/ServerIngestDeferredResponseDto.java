package eu.opertusmundi.common.model.ingest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerIngestDeferredResponseDto {

    private ServerIngestDeferredResponseDto(JsonNode node) {
        this.endpointsResource = node.at("/endpoints").asText();
        this.statusResource    = node.at("/status").asText();
        this.ticket            = node.at("/ticket").asText();
    }

    @JsonProperty("endpoints")
    private String endpointsResource;

    @JsonProperty("status")
    private String statusResource;

    private String ticket;

    public static ServerIngestDeferredResponseDto fromJsonNode(JsonNode node) {
        return new ServerIngestDeferredResponseDto(node);
    }

}
