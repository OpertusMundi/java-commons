package eu.opertusmundi.common.model.message.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.opertusmundi.common.model.RestResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Message collection response")
@NoArgsConstructor
public class ClientMessageThreadResponse extends RestResponse<ClientMessageThreadDto> {

    public ClientMessageThreadResponse(ClientMessageThreadDto thread, List<ClientContactDto> contacts) {
        super(thread);

        this.contacts = new HashMap<UUID, ClientContactDto>();

        contacts.stream().forEach(r -> {
            if (!this.contacts.containsKey(r.getId())) {
                this.contacts.put(r.getId(), r);
            }
        });
    }

    @Schema(description = "Map with all contacts (sender/recipient) for all messages in the response. The key is the contract key.")
    @Getter
    @Setter
    private Map<UUID, ClientContactDto> contacts;

}
