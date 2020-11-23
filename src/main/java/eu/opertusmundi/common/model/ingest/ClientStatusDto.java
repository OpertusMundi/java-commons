package eu.opertusmundi.common.model.ingest;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientStatusDto {

    private ClientStatusDto(JsonNode node) {
        this.comment   = node.at("/comment").asText();
        this.completed = node.at("/completed").asBoolean();
        this.success   = node.at("/success").asBoolean();
    }

    private final String comment;

    private final boolean completed;

    private final boolean success;

    public static ClientStatusDto fromJsonNode(JsonNode node) {
        return new ClientStatusDto(node);
    }

}
