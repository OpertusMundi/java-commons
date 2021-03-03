package eu.opertusmundi.common.model.message.server;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServerNotificationDto extends ServerBaseMessageDto {

    private String eventType;

    private JsonNode data;

}
