package eu.opertusmundi.common.model.message.server;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerNotificationCommandDto extends ServerBaseMessageCommandDto {

    public ServerNotificationCommandDto() {
        this.type = EnumMessageType.NOTIFICATION;
    }

    @Builder
    public ServerNotificationCommandDto(UUID recipient, String text, String eventType, JsonNode data) {
        super(EnumMessageType.NOTIFICATION, recipient, text);

        this.eventType = eventType;
        this.data      = data;
    }

    private String eventType;

    private JsonNode data;

}
