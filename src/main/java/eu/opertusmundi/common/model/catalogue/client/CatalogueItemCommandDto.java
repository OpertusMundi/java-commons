package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.asset.AssetAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.BundleAssetResourceDto;
import eu.opertusmundi.common.model.asset.EnumAssetAdditionalResource;
import eu.opertusmundi.common.model.asset.EnumResourceType;
import eu.opertusmundi.common.model.asset.FileResourceDto;
import eu.opertusmundi.common.model.asset.ResourceDto;
import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.catalogue.integration.Extensions;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeatureProperties;
import eu.opertusmundi.common.model.openapi.schema.AssetEndpointTypes;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.CallBlockRatePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.CallPrePaidPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.FixedPopulationPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.FixedPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.FixedRowPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.FreePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.RowBlockRatePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.RowPrePaidPricingModelCommandDto;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class CatalogueItemCommandDto extends BaseCatalogueItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public CatalogueItemCommandDto() {
        this.additionalResources = new ArrayList<>();
        this.pricingModels       = new ArrayList<>();
        this.resources           = new ArrayList<>();
        this.sampleAreas         = new ArrayList<>();
        this.visibility          = new ArrayList<>();
    }

    public CatalogueItemCommandDto(CatalogueFeature feature) {
        super(feature);

        final CatalogueFeatureProperties props = feature.getProperties();

        this.additionalResources = new ArrayList<>();
        this.extensions          = props.getExtensions();
        this.pricingModels       = new ArrayList<>();
        this.resources           = new ArrayList<>();
        this.sampleAreas         = new ArrayList<>();
        this.title               = props.getTitle();
        this.type                = EnumAssetType.fromString(props.getType());
        this.version             = props.getVersion();
        this.visibility          = props.getVisibility();
    }

    /**
     * The publisher key
     *
     * If this is a vendor account (with role `ROLE_VENDOR_PROVIDER`), the
     * publisher key is the unique key of the parent account. If this is a
     * provider account (with role `ROLE_PROVIDER`), this is the unique key of
     * the authenticated user.
     */
    @JsonIgnore
    private UUID publisherKey;

    /**
     * The authenticated user key
     */
    @JsonIgnore
    private UUID ownerKey;

    /**
     * Asset unique key. This value is injected by the controller.
     */
    @JsonIgnore
    private UUID draftKey;

    /**
     * True if the record must be locked when the command executes
     */
    @JsonIgnore
    private boolean locked;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Auxiliary files or additional resources to the dataset"
        ),
        minItems = 0,
        uniqueItems = true,
        schema = @Schema(implementation = AssetEndpointTypes.AssetAdditionalResource.class)
    )
    private List<AssetAdditionalResourceDto> additionalResources;

    @Schema(description = "Contract template key")
    private UUID contractTemplateKey;

    @Schema(description = "Collection of custom properties required for external data provider integration")
    @JsonInclude(Include.NON_NULL)
    @Valid
    private Extensions extensions;

    @Schema(
        description = "True if the resource files should be imported into PostGIS database and published using WMS/WFS "
                    + "endpoints. Ingest operation is only supported for formats of category `VECTOR`",
        required = false,
        defaultValue = "false",
        externalDocs = @ExternalDocumentation(
            description = "See configuration endpoint for asset file types details",
            url   = "#operation/configuration-01"
        )
    )
    private boolean ingested = false;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Supported pricing models"
        ),
        minItems = 1,
        uniqueItems = true,
        schema = @Schema(oneOf = {
            FreePricingModelCommandDto.class,
            FixedPricingModelCommandDto.class,
            FixedRowPricingModelCommandDto.class,
            FixedPopulationPricingModelCommandDto.class,
            CallPrePaidPricingModelCommandDto.class,
            CallBlockRatePricingModelCommandDto.class,
            RowPrePaidPricingModelCommandDto.class,
            RowBlockRatePricingModelCommandDto.class,
        })
    )
    @Valid
    private List<BasePricingModelCommandDto> pricingModels;

    @Schema(description = "A name given to the resource", required = true)
    @NotEmpty
    private String title;

    @Schema(description = "The nature or genre of the resource", required = true)
    @NotNull
    private EnumAssetType type;

    @Schema(description = "Version of the resource", required = true)
    @NotEmpty
    private String version;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Controls automated metadata property visibility"
        ),
        minItems = 0,
        uniqueItems = true
    )
    private List<String> visibility;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Provides a list of resources of the dataset"
        ),
        minItems = 0,
        uniqueItems = true,
        schema = @Schema(oneOf = {
            BundleAssetResourceDto.class, FileResourceDto.class, ServiceResourceDto.class
        })
    )
    private List<ResourceDto> resources;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Areas of interest (bounding boxes) for data sampling"
        ),
        minItems = 0,
        uniqueItems = true
    )
    private List<ServiceResourceSampleAreaDto> sampleAreas;

    public CatalogueFeature toFeature() {
        return new CatalogueFeature(this);
    }

    public void addFileResource(FileResourceDto resource) {
        final ResourceDto existing = this.resources.stream()
            .filter(r -> r.getId().equals(resource.getId()))
            .findFirst()
            .orElse(null);

        if (existing == null) {
            this.resources.add(resource);
        } else {
            existing.patch(resource);
        }
    }

    public void addServiceResource(ServiceResourceDto resource) {
        final ResourceDto existing = this.resources.stream()
            .filter(r -> r.getType() == EnumResourceType.SERVICE)
            .map(r -> (ServiceResourceDto) r)
            .filter(r -> r.getEndpoint().equals(resource.getEndpoint()))
            .findFirst()
            .orElse(null);

        if (existing == null) {
            this.resources.add(resource);
        } else {
            existing.patch(resource);
        }
    }

    public void addAdditionalResource(AssetFileAdditionalResourceDto resource) {
        final AssetFileAdditionalResourceDto existing = this.additionalResources.stream()
                .filter(r -> r.getType() == EnumAssetAdditionalResource.FILE)
                .map(r -> (AssetFileAdditionalResourceDto) r)
                .filter(r -> r.getId().equals(resource.getId()))
                .findFirst()
                .orElse(null);

            if (existing == null) {
                this.additionalResources.add(resource);
            } else {
                existing.patch(resource);
            }
    }

    public void addServiceResourceSampleAreas(String id, List<Geometry> areas) {
        final ServiceResourceSampleAreaDto existing = this.sampleAreas.stream()
            .filter(r -> r.getId().equalsIgnoreCase(id))
            .findFirst()
            .orElse(null);

        if (existing == null) {
            this.sampleAreas.add(ServiceResourceSampleAreaDto.of(id, areas));
        } else {
            existing.setAreas(areas);
        }
    }

}
