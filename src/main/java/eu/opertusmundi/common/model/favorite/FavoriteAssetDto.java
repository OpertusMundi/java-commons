package eu.opertusmundi.common.model.favorite;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteAssetDto extends FavoriteDto {

    private String assetId;

    private String assetVersion;

    @Hidden
    @JsonInclude(Include.NON_NULL)
    private CatalogueItemDetailsDto asset;

}
