package eu.opertusmundi.common.model.account;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountAssetDto {

    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private Integer orderId;

    @Schema(description = "Order unique key")
    private UUID orderKey;

    @JsonIgnore
    private String assetId;

    @Schema(description = "Date of purchase")
    private ZonedDateTime purchasedOn;

    @Schema(description = "Date registered to user account")
    private ZonedDateTime addedOn;

    @Schema(description = "Months of updates")
    private Integer updateInterval = 0;

    @Schema(description = "When is the last date the user is eligible for receiving updates")
    private ZonedDateTime updateEligibility;

    @Schema(description = "Operation that registered the subscription")
    private EnumAssetSource source;

    @Schema(description = "Catalogue item")
    private CatalogueItemDto item;

}
