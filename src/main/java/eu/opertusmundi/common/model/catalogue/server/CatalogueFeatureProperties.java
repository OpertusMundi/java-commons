package eu.opertusmundi.common.model.catalogue.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.asset.AssetAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.asset.ResourceDto;
import eu.opertusmundi.common.model.catalogue.client.BaseCatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemStatistics;
import eu.opertusmundi.common.model.catalogue.client.EnumConformity;
import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import eu.opertusmundi.common.model.catalogue.integration.Extensions;
import eu.opertusmundi.common.model.ingest.ResourceIngestionDataDto;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.util.StreamUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CatalogueFeatureProperties {

    public CatalogueFeatureProperties(CatalogueItemCommandDto command) {
        this.abstractText                 = command.getAbstractText();
        this.automatedMetadata            = command.getAutomatedMetadata() == null ? null : command.getAutomatedMetadata().deepCopy();
        this.conformity                   = command.getConformity() != null
            ? command.getConformity().getValue()
            : EnumConformity.NOT_EVALUATED.getValue();
        this.contractTemplateType         = command.getContractTemplateType();
        this.creationDate                 = command.getCreationDate();
        this.dateEnd                      = command.getDateEnd();
        this.dateStart                    = command.getDateStart();
        this.deliveryMethod               = command.getDeliveryMethod() != null
            ? command.getDeliveryMethod().getValue().toString()
            : null;
        this.extensions                   = command.getExtensions();
        this.format                       = command.getFormat();
        this.ingestionInfo                = command.getIngestionInfo();
        this.language                     = command.getLanguage();
        this.license                      = command.getLicense();
        this.lineage                      = command.getLineage();
        this.metadataDate                 = command.getMetadataDate();
        this.metadataLanguage             = command.getMetadataLanguage();
        this.metadataPointOfContactEmail  = command.getMetadataPointOfContactEmail();
        this.metadataPointOfContactName   = command.getMetadataPointOfContactName();
        this.openDataset                  = command.isOpenDataset();
        this.parentId                     = command.getParentId();
        this.parentDataSourceId           = command.getParentDataSourceId();
        this.publicAccessLimitations      = command.getPublicAccessLimitations();
        this.publicationDate              = command.getPublicationDate();
        this.publisherEmail               = command.getPublisherEmail();
        this.publisherId                  = command.getPublisherKey();
        this.publisherName                = command.getPublisherName();
        this.referenceSystem              = command.getReferenceSystem();
        this.resourceLocator              = command.getResourceLocator();
        this.revisionDate                 = command.getRevisionDate();
        this.spatialDataServiceType       = command.getSpatialDataServiceType() != null
            ? command.getSpatialDataServiceType().getValue()
            : null;
        this.spatialDataServiceOperations = command.getSpatialDataServiceOperations();
        this.spatialDataServiceQueryables = command.getSpatialDataServiceQueryables();
        this.spatialDataServiceVersion    = command.getSpatialDataServiceVersion();
        this.spatialResolution            = command.getSpatialResolution();
        this.statistics                   = new CatalogueItemStatistics();
        this.status                       = EnumProviderAssetDraftStatus.DRAFT.name().toLowerCase();
        this.suitableFor                  = StreamUtils.from(command.getSuitableFor()).collect(Collectors.toList());
        this.title                        = command.getTitle();
        this.type                         = command.getType() != null ? command.getType().getValue() : null;
        this.useOnlyForVas                = command.isUserOnlyForVas();
        this.version                      = command.getVersion();
        this.versions                     = Collections.emptyList();
        this.visibility                   = command.getVisibility();
        this.vettingRequired              = command.getVettingRequired();

        this.resources = StreamUtils.from(command.getResources())
            .map(ResourceDto::toCatalogueResource)
            .collect(Collectors.toList());

        this.additionalResources = StreamUtils.from(command.getAdditionalResources())
            .map(AssetAdditionalResourceDto::toCatalogueResource)
            .collect(Collectors.toList());

        this.keywords = StreamUtils.from(command.getKeywords())
            .map(Keyword::new)
            .collect(Collectors.toList());

        this.responsibleParty = StreamUtils.from(command.getResponsibleParty())
            .map(ResponsibleParty::from)
            .collect(Collectors.toList());

        // Store only pricing model parameters. The effective price will be
        // computed by the user of the object
        this.pricingModels = StreamUtils.from(command.getPricingModels())
            .collect(Collectors.toList());

        this.scales= StreamUtils.from(command.getScales())
            .map(Scale::new)
            .collect(Collectors.toList());

        this.topicCategory = StreamUtils.from(command.getTopicCategory())
            .map(EnumTopicCategory::getValue)
            .collect(Collectors.toList());
    }

    @JsonProperty("abstract")
    @JsonInclude(Include.NON_EMPTY)
    private String abstractText;

    @JsonProperty("additional_resources")
    private List<CatalogueAdditionalResource> additionalResources;

    @JsonProperty("automated_metadata")
    @JsonInclude(Include.NON_NULL)
    private JsonNode automatedMetadata;

    @JsonInclude(Include.NON_EMPTY)
    private String conformity;

    @JsonProperty("contract_template_id")
    @JsonInclude(Include.NON_NULL)
    private Integer contractTemplateId;

    @JsonProperty("contract_template_version")
    @JsonInclude(Include.NON_EMPTY)
    private String contractTemplateVersion;

    @JsonProperty("contract_template_type")
    @JsonInclude(Include.NON_NULL)
    private EnumContractType contractTemplateType = EnumContractType.MASTER_CONTRACT;

    @JsonProperty("creation_date")
    @JsonInclude(Include.NON_EMPTY)
    private String creationDate;

    @JsonProperty("date_end")
    @JsonInclude(Include.NON_EMPTY)
    private String dateEnd;

    @JsonProperty("date_start")
    @JsonInclude(Include.NON_EMPTY)
    private String dateStart;

    @JsonProperty("delivery_method")
    @JsonInclude(Include.NON_NULL)
    private String deliveryMethod;

    @JsonInclude(Include.NON_NULL)
    private Extensions extensions;

    @JsonInclude(Include.NON_EMPTY)
    private String format;

    @JsonProperty("ingestion_info")
    @JsonInclude(Include.NON_NULL)
    private List<ResourceIngestionDataDto> ingestionInfo;

    private List<Keyword> keywords;

    @JsonInclude(Include.NON_EMPTY)
    private String language;

    @JsonInclude(Include.NON_EMPTY)
    private String license;

    @JsonInclude(Include.NON_EMPTY)
    private String lineage;

    @JsonProperty("metadata_date")
    @JsonInclude(Include.NON_EMPTY)
    private String metadataDate;

    @JsonProperty("metadata_language")
    @JsonInclude(Include.NON_EMPTY)
    private String metadataLanguage;

    @JsonProperty("metadata_point_of_contact_email")
    @JsonInclude(Include.NON_EMPTY)
    private String metadataPointOfContactEmail;

    @JsonProperty("metadata_point_of_contact_name")
    @JsonInclude(Include.NON_EMPTY)
    private String metadataPointOfContactName;

    @JsonProperty("open_dataset")
    private boolean openDataset;

    @JsonProperty("parent_id")
    @JsonInclude(Include.NON_EMPTY)
    private String parentId;

    @JsonProperty("parent_data_source_id")
    @JsonInclude(Include.NON_EMPTY)
    private String parentDataSourceId;

    @JsonProperty("pricing_models")
    private List<BasePricingModelCommandDto> pricingModels;

    @JsonProperty("public_access_limitations")
    @JsonInclude(Include.NON_EMPTY)
    private String publicAccessLimitations;

    @JsonProperty("publication_date")
    @JsonInclude(Include.NON_EMPTY)
    private String publicationDate;

    @JsonProperty("publisher_email")
    @JsonInclude(Include.NON_EMPTY)
    private String publisherEmail;

    @JsonProperty("publisher_id")
    private UUID publisherId;

    @JsonProperty("publisher_name")
    @JsonInclude(Include.NON_EMPTY)
    private String publisherName;

    @JsonProperty("reference_system")
    @JsonInclude(Include.NON_EMPTY)
    private String referenceSystem;

    private List<CatalogueResource> resources;

    @JsonProperty("resource_locator")
    @JsonInclude(Include.NON_EMPTY)
    private String resourceLocator;

    @JsonProperty("responsible_party")
    @JsonInclude(Include.NON_EMPTY)
    private List<ResponsibleParty> responsibleParty;

    @JsonProperty("revision_date")
    @JsonInclude(Include.NON_EMPTY)
    private String revisionDate;

    private List<Scale> scales;

    private String status;

    @JsonProperty("spatial_data_service_type")
    @JsonInclude(Include.NON_EMPTY)
    private String spatialDataServiceType;

    @JsonProperty("spatial_data_service_version")
    @JsonInclude(Include.NON_EMPTY)
    private String spatialDataServiceVersion;

    @JsonProperty("spatial_data_service_operations")
    @JsonInclude(Include.NON_NULL)
    private List<String> spatialDataServiceOperations;

    @JsonProperty("spatial_data_service_queryables")
    @JsonInclude(Include.NON_NULL)
    private List<String> spatialDataServiceQueryables;

    @JsonProperty("spatial_resolution")
    @JsonInclude(Include.NON_NULL)
    private Integer spatialResolution;

    @JsonProperty("statistics")
    @JsonInclude(Include.NON_NULL)
    private CatalogueItemStatistics statistics;

    @JsonProperty("suitable_for")
    private List<String> suitableFor;

    private String title;

    @JsonProperty("topic_category")
    private List<String> topicCategory;

    private String type;

    @JsonProperty("use_only_for_vas")
    private boolean useOnlyForVas;

    private String version;

    private List<String> versions;

    private List<String> visibility = new ArrayList<>();

    @JsonInclude(Include.NON_NULL)
    @JsonProperty("vetting_required")
    private Boolean vettingRequired;

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Keyword {

        public Keyword(BaseCatalogueItemDto.Keyword k) {
            this.keyword = k.getKeyword();
            this.theme   = k.getTheme();
        }

        private String keyword;

        @JsonInclude(Include.NON_NULL)
        private String theme;

    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Scale {

        public Scale(BaseCatalogueItemDto.Scale s) {
            this.scale       = s.getScale();
            this.description = s.getDescription();
        }

        private Integer scale;

        @JsonInclude(Include.NON_NULL)
        private String description;

    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class ResponsibleParty {

        private String name;

        @JsonProperty("organization_name")
        @JsonInclude(Include.NON_EMPTY)
        private String organizationName;

        @JsonInclude(Include.NON_EMPTY)
        private String email;

        @JsonInclude(Include.NON_EMPTY)
        private String phone;

        @JsonInclude(Include.NON_EMPTY)
        private String address;

        @JsonProperty("service_hours")
        @JsonInclude(Include.NON_EMPTY)
        private String serviceHours;

        @JsonInclude(Include.NON_EMPTY)
        private String role;

        private ResponsibleParty(BaseCatalogueItemDto.ResponsibleParty r) {
            this.address          = r.getAddress();
            this.email            = r.getEmail();
            this.name             = r.getName();
            this.organizationName = r.getOrganizationName();
            this.phone            = r.getPhone();
            this.role             = r.getRole() != null ? r.getRole().getValue() : null;
            this.serviceHours     = r.getServiceHours();
        }

        public static ResponsibleParty from(BaseCatalogueItemDto.ResponsibleParty r) {
            return r == null ? null : new ResponsibleParty(r);
        }

    }

}
