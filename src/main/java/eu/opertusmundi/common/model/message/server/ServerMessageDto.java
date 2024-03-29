package eu.opertusmundi.common.model.message.server;

import java.util.UUID;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServerMessageDto extends ServerBaseMessageDto {

    private UUID thread;

    private UUID reply;

    private String subject;

    private Integer threadCount;

    private Integer threadCountUnread;

    private ObjectNode attributes;

}
