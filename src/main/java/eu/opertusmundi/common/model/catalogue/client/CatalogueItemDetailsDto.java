package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.opertusmundi.common.model.account.ProviderDto;
import eu.opertusmundi.common.model.asset.AssetAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.ResourceDto;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeatureProperties;
import eu.opertusmundi.common.model.contract.ContractDto;
import eu.opertusmundi.common.model.contract.CustomContractDto;
import eu.opertusmundi.common.model.contract.TemplateContractDto;
import eu.opertusmundi.common.util.StreamUtils;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Catalogue item details")
public final class CatalogueItemDetailsDto extends CatalogueItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected CatalogueItemDetailsDto() {
        super();
    }

    public CatalogueItemDetailsDto(CatalogueFeature feature) {
        super(feature);

        final CatalogueFeatureProperties props = feature.getProperties();

        this.contractTemplateId      = props.getContractTemplateId();
        this.contractTemplateVersion = props.getContractTemplateVersion();
        this.contractTemplateType 	 = props.getContractTemplateType();
        this.versions                = props.getVersions();
        this.visibility              = props.getVisibility();
        this.resources               = StreamUtils.from(props.getResources())
            .map(ResourceDto::fromCatalogueResource)
            .collect(Collectors.toList());

        this.additionalResources = StreamUtils.from(props.getAdditionalResources())
            .map(AssetAdditionalResourceDto::fromCatalogueResource)
            .collect(Collectors.toList());
    }

    @ArraySchema(
        arraySchema = @Schema(
            description = "Auxiliary files or additional resources to the dataset"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @Getter
    private List<AssetAdditionalResourceDto> additionalResources;

    @JsonIgnore
    @Getter
    @Setter
    private Integer contractTemplateId;

    @JsonIgnore
    @Getter
    @Setter
    private String contractTemplateVersion;

    @JsonIgnore
    @Getter
    @Setter
    private EnumContractType contractTemplateType;

    @Schema(description = "Contract details")
    @JsonProperty(access = Access.READ_ONLY)
    @Getter
    private ContractDto contract;

    public void setContract(TemplateContractDto contract) {
        Assert.isTrue(contract.getId().equals(this.contractTemplateId), "Contract identifier mismatch");
        Assert.isTrue(contract.getVersion().equals(this.contractTemplateVersion), "Contract identifier mismatch");

        this.contract = contract;
    }

    public void setContract(CustomContractDto contract) {
        this.contract = contract;
    }

    public void resetContract() {
        this.contract = null;
    }

    @Schema(description = "The unique key of the favorite record, if the asset is already added to the user's favorite list")
    @Getter
    @Setter
    private UUID favorite;

    @Schema(description = "`true` if the current user owns the specific asset")
    @JsonInclude(Include.NON_NULL)
    @Getter
    @Setter
    private Boolean owned;

    @Schema(description = "Publisher details")
    @JsonProperty(access = Access.READ_ONLY)
    @Getter
    private ProviderDto publisher;

    public void setPublisher(ProviderDto publisher) {
        Assert.isTrue(publisher.getKey().equals(this.publisherId), "Provider account key does not match publisher id");

        this.publisher = publisher;
    }

    @ArraySchema(
        arraySchema = @Schema(
            description = "Provides a list of resources of the dataset"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @Getter
    private List<ResourceDto> resources;

    @Schema(description = "A list of all item versions")
    @Getter
    @Setter
    private List<String> versions;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Controls automated metadata property visibility"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @Getter
    @Setter
    @JsonInclude(Include.NON_NULL)
    private List<String> visibility = new ArrayList<>();

}
