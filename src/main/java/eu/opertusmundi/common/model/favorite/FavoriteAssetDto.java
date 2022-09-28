package eu.opertusmundi.common.model.favorite;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteAssetDto extends FavoriteDto {

    @JsonIgnore
    private String assetId;

    @JsonIgnore
    private String assetVersion;

    @JsonIgnore
    private Integer assetProvider;

    @JsonIgnore
    private boolean notificationSent;

    @JsonIgnore
    private ZonedDateTime notificationSentAt;

    @Schema(description = "User action that created the asset favorite record")
    private EnumAssetFavoriteAction action;

    @Schema(description =
        "Asset details. Property `automatedMetadata` is not returned, even if the user is "
      + "eligible to view it i.e. the user is registered and authenticated."
    )
    private CatalogueItemDetailsDto asset;

}
