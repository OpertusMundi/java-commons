package eu.opertusmundi.common.model.favorite;

import java.io.Serializable;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteProviderCommandDto extends FavoriteCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected FavoriteProviderCommandDto() {
        super(EnumFavoriteType.PROVIDER);
    }

    @Schema(description = "Publisher key", required = true)
    @NotNull
    private UUID publisherKey;

}
