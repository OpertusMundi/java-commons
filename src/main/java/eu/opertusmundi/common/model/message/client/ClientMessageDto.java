package eu.opertusmundi.common.model.message.client;

import java.util.UUID;

import eu.opertusmundi.common.model.message.EnumMessageType;
import eu.opertusmundi.common.model.message.server.ServerMessageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Message object")
public class ClientMessageDto extends ClientBaseMessageDto {

    public ClientMessageDto() {
        super(EnumMessageType.MESSAGE);
    }

    public static ClientMessageDto from(ServerMessageDto m) {
        final ClientMessageDto c = new ClientMessageDto();

        c.setCreatedAt(m.getCreatedAt());
        c.setId(m.getId());
        c.setSender(m.getSender());
        c.setRead(m.isRead());
        c.setReadAt(m.getReadAt());
        c.setRecipient(m.getRecipient());
        c.setText(m.getText());
        c.setThread(m.getThread());

        return c;
    }

    @Schema(description = "Message thread unique id")
    @Getter
    @Setter
    private UUID thread;

}
