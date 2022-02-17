package eu.opertusmundi.common.model.rating.server;

import eu.opertusmundi.common.model.rating.client.ClientRatingCommandDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ServerAssetRatingCommandDto extends ServerBaseRatingCommandDto {

    public ServerAssetRatingCommandDto(ClientRatingCommandDto c) {
        this.comment = c.getComment();
        this.value   = c.getValue();
    }

}
