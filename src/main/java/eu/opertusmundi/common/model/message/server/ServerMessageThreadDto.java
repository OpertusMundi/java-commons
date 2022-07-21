package eu.opertusmundi.common.model.message.server;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServerMessageThreadDto {

    private UUID key;

    private UUID owner;

    private String subject;

    private String text;

    private ZonedDateTime modifiedAt;

    private Integer count;

    private Integer unread;

    private List<ServerMessageDto> messages = new ArrayList<>();

}
