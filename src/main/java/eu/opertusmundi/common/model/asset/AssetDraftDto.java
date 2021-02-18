package eu.opertusmundi.common.model.asset;

import java.time.ZonedDateTime;
import java.util.UUID;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.dto.PublisherDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetDraftDto {

    @Schema(description=
          "Draft unique identifier. "
        + "Also the key for the catalogue draft record. "
        + "If the draft is submitted and a workflow instance "
        + "is initialized, it is used as the business key"
    )
    private UUID key;

    @Schema(description = "Asset title")
    private String title;

    @Schema(description = "Asset version")
    private String version;

    @Schema(description = "Draft data", implementation = CatalogueItemCommandDto.class)
    private CatalogueItemCommandDto command;

    @Schema(description = "Catalogue draft id. Always equal to key property")
    private UUID assetDraft;

    @Schema(description = "Published asset id. The value is generated by the PID service")
    private String assetPublished;

    @Schema(description = "Draft status")
    private EnumProviderAssetDraftStatus status;

    @Schema(description = "Rejection reason if the draft has been rejected by a HelpDesk user")
    private String helpdeskRejectionReason;

    @Schema(description = "Rejection reason if the draft has been rejected by the provider")
    private String providerRejectionReason;

    @Schema(
        description = "True if the file should be imported into PostGIS database and published using WMS/WFS endpoints"
    )
    private boolean ingested = false;

    @Schema(description = "Creation date in ISO format")
    private ZonedDateTime createdOn;

    @Schema(description = "Date of lat update in ISO format")
    private ZonedDateTime modifiedOn;
    
    @Schema(description = "Publisher details")
    private PublisherDto publisher;
    
    public AssetResourceDto getResourceByKey(UUID key) {
        return this.getCommand().getResources().stream()
            .filter(r -> r.getId().equals(key))
            .findFirst()
            .orElse(null);
    }

}
