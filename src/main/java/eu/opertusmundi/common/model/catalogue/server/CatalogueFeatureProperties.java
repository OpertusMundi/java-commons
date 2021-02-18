package eu.opertusmundi.common.model.catalogue.server;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.catalogue.client.BaseCatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemStatistics;
import eu.opertusmundi.common.model.catalogue.client.EnumConformity;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CatalogueFeatureProperties {

    public CatalogueFeatureProperties(CatalogueItemCommandDto command) {
        this.abstractText                = command.getAbstractText();
        this.automatedMetadata           = command.getAutomatedMetadata().deepCopy();
        this.conformity                  = command.getConformity() != null 
            ? command.getConformity().getValue()
            : EnumConformity.NOT_EVALUATED.getValue();
        this.creationDate                = command.getCreationDate();
        this.dateEnd                     = command.getDateEnd();
        this.dateStart                   = command.getDateStart();
        this.format                      = command.getFormat();
        this.language                    = command.getLanguage();
        this.license                     = command.getLicense();
        this.lineage                     = command.getLineage();
        this.metadataDate                = command.getMetadataDate();
        this.metadataLanguage            = command.getMetadataLanguage();
        this.metadataPointOfContactEmail = command.getMetadataPointOfContactEmail();
        this.metadataPointOfContactName  = command.getMetadataPointOfContactName();
        this.parentId                    = command.getParentId();
        this.publicAccessLimitations     = command.getPublicAccessLimitations();
        this.publicationDate             = command.getPublicationDate();
        this.publisherEmail              = command.getPublisherEmail();
        this.publisherId                 = command.getPublisherKey();
        this.publisherName               = command.getPublisherName();
        this.referenceSystem             = command.getReferenceSystem();
        this.resourceLocator             = command.getResourceLocator();
        this.revisionDate                = command.getRevisionDate();
        this.spatialDataServiceType      = command.getSpatialDataServiceType() != null
            ? command.getSpatialDataServiceType().getValue()
            : null;
        this.spatialResolution           = command.getSpatialResolution();
        this.statistics                  = new CatalogueItemStatistics();
        this.status                      = EnumProviderAssetDraftStatus.DRAFT.name().toLowerCase();
        this.suitableFor                 = this.toStream(command.getSuitableFor()).collect(Collectors.toList());
        this.title                       = command.getTitle();
        this.type                        = command.getType() != null ? command.getType().getValue() : null;
        this.version                     = command.getVersion();
        this.versions                    = Collections.emptyList();

        this.additionalResources = this.toStream(command.getAdditionalResources())
            .map(r -> r.toCatalogueResource())
            .collect(Collectors.toList());
      
        this.keywords = this.toStream(command.getKeywords())
            .map(Keyword::new)
            .collect(Collectors.toList());
        
        // Resources are stored by the asset data repository
        final List<CatalogueResource> feeatureResources = this.toStream(command.getResources())
            .map(r -> r.toCatalogueResource())
            .collect(Collectors.toList());
        
        this.resources = feeatureResources;  

        // Store only pricing model parameters. The effective price will be
        // computed by the user of the object
        this.pricingModels = this.toStream(command.getPricingModels()).collect(Collectors.toList());
               
        this.scales= this.toStream(command.getScales())
            .map(Scale::new)
            .collect(Collectors.toList());
       
        this.topicCategory = this.toStream(command.getTopicCategory())
            .map(EnumTopicCategory::getValue)
            .collect(Collectors.toList());
    }

    @JsonProperty("abstract")
    private String abstractText;

    @JsonProperty("additional_resources")
    private List<CatalogueAdditionalResource> additionalResources;

    @JsonProperty("automated_metadata")
    private JsonNode automatedMetadata;

    private String conformity;

    @JsonProperty("creation_date")
    private String creationDate;

    @JsonProperty("date_end")
    private String dateEnd;

    @JsonProperty("date_start")
    private String dateStart;

    private String format;

    private List<Keyword> keywords;

    private String language;

    private String license;

    private String lineage;

    @JsonProperty("metadata_date")
    private String metadataDate;

    @JsonProperty("metadata_language")
    private String metadataLanguage;

    @JsonProperty("metadata_point_of_contact_email")
    private String metadataPointOfContactEmail;

    @JsonProperty("metadata_point_of_contact_name")
    private String metadataPointOfContactName;

    @JsonProperty("parent_id")
    private String parentId;

    @JsonProperty("pricing_models")
    private List<BasePricingModelCommandDto> pricingModels;

    @JsonProperty("public_access_limitations")
    private String publicAccessLimitations;

    @JsonProperty("publication_date")
    private String publicationDate;

    @JsonProperty("publisher_email")
    private String publisherEmail;

    @JsonProperty("publisher_id")
    private UUID publisherId;

    @JsonProperty("publisher_name")
    private String publisherName;

    @JsonProperty("reference_system")
    private String referenceSystem;

    private List<CatalogueResource> resources;

    @JsonProperty("resource_locator")
    private String resourceLocator;

    @JsonProperty("revision_date")
    private String revisionDate;

    private List<Scale> scales;

    private String status;

    @JsonProperty("spatial_data_service_type")
    @JsonInclude(Include.NON_NULL)
    private String spatialDataServiceType;

    @JsonProperty("spatial_resolution")
    private Integer spatialResolution;

    @JsonProperty("statistics")
    private CatalogueItemStatistics statistics;

    @JsonProperty("suitable_for")
    private List<String> suitableFor;

    private String title;

    @JsonProperty("topic_category")
    private List<String> topicCategory;

    private String type;

    private String version;
    
    private List<String> versions;

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Keyword {
               
        public Keyword(BaseCatalogueItemDto.Keyword k) {
            this.keyword = k.getKeyword();
            this.theme   = k.getTheme();
        }

        private String keyword;

        private String theme;

    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Scale {

        public Scale(BaseCatalogueItemDto.Scale s) {
            this.scale = s.getScale();
            this.theme = s.getTheme();
        }
        
        private Integer scale;

        private String theme;

    }
    
    private <T> Stream<T> toStream(Collection<T> collection) {
        return Optional.ofNullable(collection)
          .map(Collection::stream)
          .orElseGet(Stream::empty);
    }

}
