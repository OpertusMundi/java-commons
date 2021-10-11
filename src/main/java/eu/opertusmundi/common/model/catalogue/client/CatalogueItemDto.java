package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeatureProperties;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import io.jsonwebtoken.lang.Assert;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class CatalogueItemDto extends BaseCatalogueItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public CatalogueItemDto(CatalogueFeature feature) {
        super(feature);

        final CatalogueFeatureProperties props = feature.getProperties();

        this.id = feature.getId();

        this.pricingModels = Optional.ofNullable(props.getPricingModels()).orElse(Collections.emptyList());
        this.publisherId   = props.getPublisherId();
        this.statistics    = props.getStatistics();
        this.title         = props.getTitle();
        this.type          = EnumAssetType.fromString(props.getType());
        this.version       = props.getVersion();

        // Initialize with an empty collection. Caller must compute the
        // effective pricing models
        this.effectivePricingModels = new ArrayList<EffectivePricingModelDto>();

        // Reset geometry for tabular data
        if (this.getType() == EnumAssetType.TABULAR) {
            this.setGeometry(null);
        }
    }

    @Schema(description = "Catalogue item identifier (PID)")
    @Getter
    @Setter
    private String id;

    @JsonIgnore
    @Getter
    @Setter
    private List<BasePricingModelCommandDto> pricingModels;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Supported pricing models"
        ),
        minItems = 1,
        uniqueItems = true,
        schema = @Schema(implementation = EffectivePricingModelDto.class)
    )
    @Getter
    @JsonProperty(value = "pricingModels", access = Access.READ_ONLY)
    @JsonInclude(Include.NON_EMPTY)
    private List<EffectivePricingModelDto> effectivePricingModels;

    @JsonIgnore
    public void setEffectivePricingModels(List<EffectivePricingModelDto> models) {
        Assert.notNull(models, "Expected a non-null list of effective pricing models");
        this.effectivePricingModels = models;
    }

    @Schema(description = "Id of an entity responsible for making the resource available")
    @Getter
    @Setter
    protected UUID publisherId;

    @Schema(description = "Asset statistics")
    @Getter
    @Setter
    private CatalogueItemStatistics statistics;

    @Schema(description = "A name given to the resource")
    @Getter
    @Setter
    private String title;

    @Schema(description = "The nature or genre of the resource")
    @Getter
    @Setter
    private EnumAssetType type;

    @Schema(description = "Version of the resource")
    @Getter
    @Setter
    private String version;

}
