package eu.opertusmundi.common.model.rating.server;

import eu.opertusmundi.common.model.rating.client.ClientRatingCommandDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ServerProviderRatingCommandDto extends ServerBaseRatingCommandDto {

    public ServerProviderRatingCommandDto(ClientRatingCommandDto c) {
        this.comment = c.getComment();
        this.value   = c.getValue();
    }

}
