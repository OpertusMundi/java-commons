package eu.opertusmundi.common.model.catalogue.client;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeatureProperties;
import eu.opertusmundi.common.model.openapi.schema.GeometryAsJson;
import eu.opertusmundi.common.util.StreamUtils;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseCatalogueItemDto {

    protected BaseCatalogueItemDto() {
        this.keywords      = Collections.emptyList();
        this.scales        = Collections.emptyList();
        this.suitableFor   = Collections.emptyList();
        this.topicCategory = Collections.emptyList();
    }

    protected BaseCatalogueItemDto(CatalogueFeature feature) {
        final CatalogueFeatureProperties props = feature.getProperties();

        this.abstractText                = props.getAbstractText();
        this.automatedMetadata           = props.getAutomatedMetadata();
        this.conformity                  = EnumConformity.fromString(props.getConformity());
        this.creationDate                = props.getCreationDate();
        this.dateEnd                     = props.getDateEnd();
        this.dateStart                   = props.getDateStart();
        this.deliveryMethod              = EnumDeliveryMethod.fromString(props.getDeliveryMethod());
        this.format                      = props.getFormat();
        this.language                    = props.getLanguage();
        this.license                     = props.getLicense();
        this.lineage                     = props.getLineage();
        this.metadataDate                = props.getMetadataDate();
        this.metadataLanguage            = props.getMetadataLanguage();
        this.metadataPointOfContactEmail = props.getMetadataPointOfContactEmail();
        this.metadataPointOfContactName  = props.getMetadataPointOfContactName();
        this.parentId                    = props.getParentId();
        this.publicAccessLimitations     = props.getPublicAccessLimitations();
        this.publicationDate             = props.getPublicationDate();
        this.publisherEmail              = props.getPublisherEmail();
        this.publisherName               = props.getPublisherName();
        this.referenceSystem             = props.getReferenceSystem();
        this.resourceLocator             = props.getResourceLocator();
        this.revisionDate                = props.getRevisionDate();
        this.spatialDataServiceType      = EnumSpatialDataServiceType.fromString(props.getSpatialDataServiceType());
        this.spatialResolution           = props.getSpatialResolution();
        this.suitableFor                 = props.getSuitableFor();

        this.geometry = feature.getGeometry();

        this.keywords = StreamUtils.from(props.getKeywords())
            .map(Keyword::new)
            .collect(Collectors.toList());
               
        this.scales = StreamUtils.from(props.getScales())
            .map(Scale::new)
            .collect(Collectors.toList());

        this.topicCategory = StreamUtils.from(props.getTopicCategory())
            .map(EnumTopicCategory::fromString)
            .collect(Collectors.toList());
    }

    @Schema(description = "An abstract of the resource")
    @JsonProperty("abstract")
    private String abstractText;
    
	@Schema(description = "Automated metadata")
	@JsonInclude(Include.NON_NULL)
	private JsonNode automatedMetadata;
    
    @Schema(description = "Degree of conformity with the implementing rules/standard of the metadata followed")
    private EnumConformity conformity;

    @Schema(
        description = "A point or period of time associated with the creation event in the lifecycle of the resource",
        example = "2020-06-02"
    )
    private String creationDate;

    @Schema(description = "The temporal extent of the resource (end date)", example = "2020-06-02")
    private String dateEnd;

    @Schema(description = "The temporal extent of the resource (start date)", example = "2020-06-02")
    private String dateStart;

    @Schema(description = "Channel of asset distribution")
    private EnumDeliveryMethod deliveryMethod;
    
    @Schema(description = "The file format, physical medium, or dimensions of the resource", example = "ESRI Shapefile")
    private String format;

    @Schema(implementation = GeometryAsJson.class, description = "Geometry as GeoJSON")
    private Geometry geometry;
    
    @Schema(description = "The topic of the resource")
    @ArraySchema(
        arraySchema = @Schema(
            description = "Resource Keywords"
        ),
        minItems = 0
    )
    private List<Keyword> keywords;

    @Schema(
        description = "A language of the resource as an ISO 639-1 two-letter code",
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes")
    )
    private String language;

    @Schema(description = "Information about resource licensing")
    private String license;

    @Schema(description = "General explanation of the data producer's knowledge about the lineage of a dataset")
    private String lineage;

    @Schema(description = "The date which specifies when the metadata record was created or updated", example = "2020-06-02")
    private String metadataDate;

    @Schema(
        description = "The language in which the metadata elements are expressed as a ISO 639-1 two-letter code", 
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes")
    )
    private String metadataLanguage;

    @Schema(
        description = "The email of the organization responsible for the creation and maintenance of the metadata"
    )
    private String metadataPointOfContactEmail;

    @Schema(
        description = "The name of the organization responsible for the creation and maintenance of the metadata"
    )
    private String metadataPointOfContactName;

    @Schema(description = "Provides the ID of a parent dataset")
    private String parentId;

    @Schema(description = "Information on the limitations and the reasons for them")
    private String publicAccessLimitations;

    @Schema(
        description = "A point or period of time associated with the publication even in the "
                    + "lifecycle of the resource",
        example = "2020-06-02"
    )
    private String publicationDate;

    @Schema(description = "Email of an entity responsible for making the resource available")
    private String publisherEmail;

    @Schema(description = "Name of an entity responsible for making the resource available")
    private String publisherName;

    @Schema(description = "Information about the reference system", example = "EPSG:4326")
    private String referenceSystem;

    @Schema(
        description = "The 'navigation section' of a metadata record which point users to the location (URL) "
                    + "where the data can be downloaded, or to where additional information about the resource "
                    + "may be provided",
        example = ""
    )
    private String resourceLocator;    
    
    @Schema(
        description = "A point or period of time associated with the revision event in the "
                    + "lifecycle of the resource",
        example = "2020-06-02"
    )
    private String revisionDate;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Scale refers to the level of detail of the data set"
        ),
        minItems = 0
    )
    private List<Scale> scales;

    @Schema(description = "The nature or genre of the service", example = "TMS")
    private EnumSpatialDataServiceType spatialDataServiceType;

    @Schema(description = "Spatial resolution refers to the level of detail of the data set", example = "1000")
    private Integer spatialResolution;

    @ArraySchema(
        arraySchema = @Schema(
            description = "A description of geospatial analysis or processing that the dataset is suitable for"
        ),
        minItems = 0
    )
    private List<String> suitableFor;
       
    @ArraySchema(
        arraySchema = @Schema(
            description = "A high-level classification scheme to assist in the grouping and topic-based "
                        + "search of available spatial data resources",
            example = "BIOTA"
        ),
        minItems = 0
    )
    private List<EnumTopicCategory> topicCategory;

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Keyword {

        @Schema(description = "Keyword value")
        private String keyword;

        @Schema(description = "A related theme")
        private String theme;

        public Keyword(CatalogueFeatureProperties.Keyword k) {
            this.keyword = k.getKeyword();
            this.theme   = k.getTheme();
        }

    }
    
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Scale {

        @Schema(description = "Scale value")
        private Integer scale;

        @Schema(description = "A short description")
        private String theme;

        public Scale(CatalogueFeatureProperties.Scale s) {
            this.scale = s.getScale();
            this.theme = s.getTheme();
        }

    }
    
}
