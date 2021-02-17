package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.opertusmundi.common.model.asset.AssetAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetResourceDto;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeatureProperties;
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
        
        final CatalogueFeatureProperties props = feature.getProperties();
        
        this.statistics = props.getStatistics();
        this.versions   = props.getVersions();
        this.resources  = new ArrayList<AssetResourceDto>();
        
        this.additionalResources = this.toStream(props.getAdditionalResources())
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
    @Getter
    private List<AssetResourceDto> resources;
    
    @Schema(description = "Asset statistics")
    @Getter
    @Setter
    private CatalogueItemStatistics statistics;
    
    @Schema(description = "A list of all item versions")
    @Getter
    @Setter
    private List<String> versions;

    private <T> Stream<T> toStream(Collection<T> collection) {
        return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
    }
}
