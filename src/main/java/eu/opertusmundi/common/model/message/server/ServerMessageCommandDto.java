package eu.opertusmundi.common.model.message.server;

import java.util.Map;
import java.util.UUID;

import eu.opertusmundi.common.model.message.EnumMessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerMessageCommandDto extends ServerBaseMessageCommandDto {

    public ServerMessageCommandDto() {
        this.type = EnumMessageType.MESSAGE;
    }

    @Builder
    public ServerMessageCommandDto(
        UUID recipient, String subject, String text, UUID sender, UUID thread, String idempotentKey, Map<String, String> attributes
    ) {
        super(EnumMessageType.MESSAGE, recipient, text);

        this.attributes    = attributes;
        this.idempotentKey = idempotentKey;
        this.sender        = sender;
        this.subject       = subject;
        this.thread        = thread;
    }

    private Map<String, String> attributes;
    private String              idempotentKey;
    private UUID                sender;
    private String              subject;
    private UUID                thread;

}
