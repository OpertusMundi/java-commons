package eu.opertusmundi.common.model.rating.server;

import eu.opertusmundi.common.model.rating.client.ClientAssetRatingCommandDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ServerAssetRatingCommandDto extends ServerBaseRatingCommandDto {

    public ServerAssetRatingCommandDto(ClientAssetRatingCommandDto c) {
        this.comment = c.getComment();
        this.value   = c.getValue();
    }

}
