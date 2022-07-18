package eu.opertusmundi.common.model.message.client;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.message.EnumMessageType;
import eu.opertusmundi.common.model.message.server.ServerMessageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Message object")
@Getter
@Setter
public class ClientMessageDto extends ClientBaseMessageDto {

    public ClientMessageDto() {
        super(EnumMessageType.MESSAGE);
    }

    public static ClientMessageDto from(ServerMessageDto m) {
        final ClientMessageDto c = new ClientMessageDto();

        c.setCreatedAt(m.getCreatedAt());
        c.setId(m.getId());
        c.setSenderId(m.getSender());
        c.setRead(m.isRead());
        c.setReadAt(m.getReadAt());
        c.setRecipientId(m.getRecipient());
        c.setReply(m.getReply());
        c.setSubject(m.getSubject());
        c.setText(m.getText());
        c.setThread(m.getThread());

        return c;
    }

    @Schema(description = "Message recipient identifier")
    private UUID recipientId;

    @Schema(description = "Message recipient contact")
    @JsonInclude(Include.NON_NULL)
    private ClientContactDto recipient;

    @Schema(description = "Message thread unique id")
    private UUID thread;

    @Schema(description = "Reply message unique id")
    private UUID reply;

    @Schema(description = "Message subject")
    private String subject;

}
