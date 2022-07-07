package eu.opertusmundi.common.model.message.server;

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
    public ServerMessageCommandDto(UUID recipient, String text, UUID sender, UUID thread, String idempotentKey) {
        super(EnumMessageType.MESSAGE, recipient, text);

        this.sender        = sender;
        this.thread        = thread;
        this.idempotentKey = idempotentKey;
    }

    private String idempotentKey;

    private UUID sender;

    private UUID thread;

}
