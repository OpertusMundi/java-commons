package eu.opertusmundi.common.model.message.client;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.message.server.EnumMessageType;
import eu.opertusmundi.common.model.message.server.ServerNotificationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientNotificationDto extends ClientBaseMessageDto {

    public ClientNotificationDto() {
        super(EnumMessageType.NOTIFICATION);
    }

    public ClientNotificationDto(ServerNotificationDto n) {
        this();

        this.setCreatedAt(n.getCreatedAt());
        this.setData(n.getData());
        this.setEventType(n.getEventType() == null ? null : EnumNotificationType.valueOf(n.getEventType()));
        this.setId(n.getId());
        this.setRead(n.isRead());
        this.setReadAt(n.getReadAt());
        this.setRecipient(n.getRecipient());
        this.setText(n.getText());
    }

    @Schema(description = "Notification recipient")
    private UUID recipient;
    
    @Schema(description = "Event type")
    private EnumNotificationType eventType;
    
    @Schema(description = "Optional application specific data")
    private JsonNode data;

}
