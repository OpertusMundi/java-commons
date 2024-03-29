package eu.opertusmundi.common.model.message.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Message collection response")
@NoArgsConstructor
@Getter
@Setter
public class ClientMessageCollectionResponse extends RestResponse<PageResultDto<ClientMessageDto>> {

    public ClientMessageCollectionResponse(PageResultDto<ClientMessageDto> result, List<ClientContactDto> contacts) {
        super(result);

        this.contacts = new HashMap<UUID, ClientContactDto>();

        contacts.stream().forEach(r -> {
            if (!this.contacts.containsKey(r.getId())) {
                this.contacts.put(r.getId(), r);
            }
        });
    }

    @Schema(description = "Map with all contacts (sender/recipient) for all messages in the response. The key is the contract key.")
    private Map<UUID, ClientContactDto> contacts;

}
