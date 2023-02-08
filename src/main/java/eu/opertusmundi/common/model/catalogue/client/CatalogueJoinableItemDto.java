package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import io.jsonwebtoken.lang.Assert;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CatalogueJoinableItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public static CatalogueJoinableItemDto from(CatalogueItemDetailsDto item) {
        final var j = new CatalogueJoinableItemDto();

        j.effectivePricingModels = item.getEffectivePricingModels();
        j.id                     = item.getId();
        j.pricingModels          = item.getPricingModels();
        j.publisherId            = item.getPublisherId();
        j.serviceType            = item.getSpatialDataServiceType();
        j.statistics             = item.getStatistics();
        j.title                  = item.getTitle();
        j.type                   = item.getType();
        j.version                = item.getVersion();

        return j;
    }

    @Schema(description = "Catalogue item identifier (PID)")
    @Getter
    private String id;

    @JsonIgnore
    @Getter
    private List<BasePricingModelCommandDto> pricingModels;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Supported pricing models"
        ),
        minItems = 1,
        uniqueItems = true,
        schema = @Schema(implementation = EffectivePricingModelDto.class)
    )
    @JsonProperty(value = "pricingModels", access = Access.READ_ONLY)
    @JsonInclude(Include.NON_EMPTY)
    private List<EffectivePricingModelDto> effectivePricingModels;

    @JsonIgnore
    public void setEffectivePricingModels(List<EffectivePricingModelDto> models) {
        Assert.notNull(models, "Expected a non-null list of effective pricing models");
        this.effectivePricingModels = models;
    }

    @Schema(description = "Id of an entity responsible for making the resource available")
    private UUID publisherId;

    @Schema(description = "The nature or genre of the service", example = "TMS")
    private EnumSpatialDataServiceType serviceType;

    @Schema(description = "Asset statistics")
    private CatalogueItemStatistics statistics;

    @Schema(description = "A name given to the resource")
    private String title;

    @Schema(description = "The nature or genre of the resource")
    private EnumAssetType type;

    @Schema(description = "Version of the resource")
    private String version;

}
