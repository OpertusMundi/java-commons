package eu.opertusmundi.common.model.favorite;

import eu.opertusmundi.common.model.account.ProviderDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteProviderDto extends FavoriteDto {

    @Schema(description = "Provider details")
    private ProviderDto provider;

}
