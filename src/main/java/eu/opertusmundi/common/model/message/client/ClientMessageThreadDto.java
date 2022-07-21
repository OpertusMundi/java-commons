package eu.opertusmundi.common.model.message.client;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.message.server.ServerMessageThreadDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Message thread object")
@Getter
@Setter
public class ClientMessageThreadDto {

    public static ClientMessageThreadDto from(ServerMessageThreadDto s) {
        final ClientMessageThreadDto c = new ClientMessageThreadDto();

        c.setCount(s.getCount());
        c.setKey(s.getKey());
        c.setModifiedAt(s.getModifiedAt());
        c.setOwner(s.getOwner());
        c.setSubject(s.getSubject());
        c.setText(s.getText());
        c.setUnread(s.getUnread());

        s.getMessages().stream().map(ClientMessageDto::from).forEach(c.getMessages()::add);
        c.getMessages().sort((a, b)-> a.getCreatedAt().compareTo(b.getCreatedAt()));

        return c;
    }

    private UUID key;

    private UUID owner;

    private String subject;

    private String text;

    private ZonedDateTime modifiedAt;

    private Integer count;

    private Integer unread;

    private List<ClientMessageDto> messages = new ArrayList<>();

}
