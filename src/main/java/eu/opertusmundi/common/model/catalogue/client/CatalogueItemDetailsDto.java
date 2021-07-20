package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.opertusmundi.common.model.account.ProviderDto;
import eu.opertusmundi.common.model.asset.AssetAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.FileResourceDto;
import eu.opertusmundi.common.model.asset.ResourceDto;
import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeatureProperties;
import eu.opertusmundi.common.model.contract.ContractDto;
import eu.opertusmundi.common.model.openapi.schema.AssetEndpointTypes;
import eu.opertusmundi.common.util.StreamUtils;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

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
        this.statistics              = props.getStatistics();
        this.versions                = props.getVersions();
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
        uniqueItems = true,
        schema = @Schema(implementation = AssetEndpointTypes.AssetAdditionalResource.class)
    )
    @Getter
    private List<AssetAdditionalResourceDto> additionalResources;

    @JsonIgnore
    private Integer contractTemplateId;

    @JsonIgnore
    private String contractTemplateVersion;

    @Schema(description = "Contract details")
    @JsonProperty(access = Access.READ_ONLY)
    @Getter
    private ContractDto contract;

    public void setContract(ContractDto contract) {
        Assert.isTrue(contract.getId().equals(this.contractTemplateId), "Contract identifier mismatch");
        Assert.isTrue(contract.getVersion().equals(this.contractTemplateVersion), "Contract identifier mismatch");

        this.contract = contract;
    }

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
        uniqueItems = true,
        schema = @Schema(oneOf = {FileResourceDto.class, ServiceResourceDto.class})
    )
    @Getter
    private List<ResourceDto> resources;

    @Schema(description = "Asset statistics")
    @Getter
    @Setter
    private CatalogueItemStatistics statistics;

    @Schema(description = "A list of all item versions")
    @Getter
    @Setter
    private List<String> versions;

}
