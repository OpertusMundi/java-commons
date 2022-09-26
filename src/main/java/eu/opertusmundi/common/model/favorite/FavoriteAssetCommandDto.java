package eu.opertusmundi.common.model.favorite;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class FavoriteAssetCommandDto extends FavoriteCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected FavoriteAssetCommandDto() {
        super(EnumFavoriteType.ASSET);
    }

    @JsonIgnore
    private transient CatalogueItemDetailsDto item;

    @Schema(description = "Published asset unique PID", required = true)
    @NotEmpty
    private String pid;

    @Schema(description = "Favorite action", defaultValue = "FAVORITE")
    private EnumAssetFavoriteAction action = EnumAssetFavoriteAction.FAVORITE;

}
