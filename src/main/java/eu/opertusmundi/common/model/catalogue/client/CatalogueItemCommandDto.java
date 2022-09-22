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
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.asset.AssetAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetContractAnnexDto;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.EnumAssetAdditionalResource;
import eu.opertusmundi.common.model.asset.EnumResourceType;
import eu.opertusmundi.common.model.asset.FileResourceDto;
import eu.opertusmundi.common.model.asset.ResourceDto;
import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.catalogue.integration.Extensions;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeatureProperties;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
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
        this.contractAnnexes     = new ArrayList<>();
        this.pricingModels       = new ArrayList<>();
        this.resources           = new ArrayList<>();
        this.sampleAreas         = new ArrayList<>();
        this.visibility          = new ArrayList<>();
    }

    public CatalogueItemCommandDto(CatalogueFeature feature) {
        super(feature);

        final CatalogueFeatureProperties props = feature.getProperties();

        this.abstractText        = props.getAbstractText();
        this.additionalResources = new ArrayList<>();
        this.contractAnnexes     = new ArrayList<>();
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
        uniqueItems = true
    )
    private List<AssetAdditionalResourceDto> additionalResources;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Custom contract annex files"
        ),
        minItems = 0,
        uniqueItems = true
    )
    private List<AssetContractAnnexDto> contractAnnexes;

    @Schema(description = "Contract template key")
    private UUID contractTemplateKey;

    @Schema(description = "Contract type")
    private EnumContractType contractTemplateType = EnumContractType.MASTER_CONTRACT;

    @Schema(description = "Collection of custom properties required for external data provider integration")
    @JsonInclude(Include.NON_NULL)
    @Valid
    private Extensions extensions;

    @Schema(
        description = "`true` if the data profiling task should be executed during the asset publish workflow",
        defaultValue = "true"
    )
    private boolean dataProfilingEnabled = true;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Supported pricing models"
        ),
        minItems = 1,
        uniqueItems = true
    )
    @Valid
    private List<BasePricingModelCommandDto> pricingModels;

    @Schema(description = "A name given to the resource", required = true)
    @NotEmpty
    private String title;

    @Schema(description = "An abstract of the resource")
    @NotEmpty
    @JsonProperty("abstract")
    private String abstractText;

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
        uniqueItems = true
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

    public void addContractAnnexResource(AssetContractAnnexDto annex) {
        final AssetContractAnnexDto existing = this.contractAnnexes.stream()
            .filter(r -> r.getId().equals(annex.getId()))
            .findFirst()
            .orElse(null);

        if (existing == null) {
            this.contractAnnexes.add(annex);
        } else {
            existing.patch(annex);
        }
    }

}
