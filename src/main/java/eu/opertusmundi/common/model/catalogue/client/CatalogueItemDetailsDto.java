package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.List;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.opertusmundi.common.model.asset.AssetAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetResourceDto;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.dto.PublisherDto;
import eu.opertusmundi.common.model.openapi.schema.AssetEndpointTypes;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class CatalogueItemDetailsDto extends CatalogueItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected CatalogueItemDetailsDto() {
        super();
    }

    public CatalogueItemDetailsDto(CatalogueFeature feature) {
        super(feature);
        
        this.statistics  = feature.getProperties().getStatistics();
        this.versions = feature.getProperties().getVersions();
    }

    @ArraySchema(
        arraySchema = @Schema(
            description = "Auxiliary files or additional resources to the dataset"
        ),
        minItems = 0,
        uniqueItems = true,
        schema = @Schema(implementation = AssetEndpointTypes.AssetAdditionalResource.class)
    )
    private List<AssetAdditionalResourceDto> additionalResources;
    
    @Schema(description = "Publisher details")
    @JsonProperty(access = Access.READ_ONLY)
    @Getter
    private PublisherDto publisher;

    public void setPublisher(PublisherDto publisher) {
        Assert.isTrue(publisher.getKey().equals(this.publisherId), "Provider account key does not match publisher id");

        this.publisher = publisher;
    }

    @ArraySchema(
        arraySchema = @Schema(
            description = "Provides a list of resources of the dataset"
        ),
        minItems = 0,
        uniqueItems = true,
        schema = @Schema(implementation = AssetResourceDto.class)
    )
    private List<AssetResourceDto> resources;
    
    @Schema(description = "Asset statistics")
    @Getter
    @Setter
    private CatalogueItemStatistics statistics;
    
    @Schema(description = "A list of all item versions")
    @Getter
    @Setter
    private List<String> versions;

}
