package eu.opertusmundi.common.model.rating.server;

import eu.opertusmundi.common.model.rating.client.ClientProviderRatingCommandDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ServerProviderRatingCommandDto extends ServerBaseRatingCommandDto {

    public ServerProviderRatingCommandDto(ClientProviderRatingCommandDto c) {
        this.comment = c.getComment();
        this.value   = c.getValue();
    }

}
