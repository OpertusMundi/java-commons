package eu.opertusmundi.common.model.message.server;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.message.EnumMessageType;
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
    public ServerNotificationCommandDto(UUID recipient, String text, String eventType, JsonNode data, String idempotentKey) {
        super(EnumMessageType.NOTIFICATION, recipient, text);

        this.eventType     = eventType;
        this.data          = data;
        this.idempotentKey = idempotentKey;
    }

    private String idempotentKey;

    private String eventType;

    private JsonNode data;

}
