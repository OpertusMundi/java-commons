package eu.opertusmundi.common.model.asset.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.Message;
import eu.opertusmundi.common.model.account.SimpleAccountDto;
import eu.opertusmundi.common.model.account.helpdesk.SimpleHelpdeskAccountDto;
import eu.opertusmundi.common.model.ingest.ResourceIngestionDataDto;
import eu.opertusmundi.common.model.openapi.schema.GeometryAsJson;
import eu.opertusmundi.common.model.openapi.schema.UserServiceEndpointTypes;
import eu.opertusmundi.common.model.pricing.PerCallPricingModelCommandDto;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserServiceDto {

    @JsonIgnore
    private int id;

    @Schema(description = "An abstract of the resource")
    private String abstractText;

    @Schema(title = "Automated metadata", implementation = UserServiceEndpointTypes.JsonNodeMetadata.class)
    @JsonInclude(Include.NON_NULL)
    private JsonNode automatedMetadata;

    @Schema(description = "`True` if the geometry has been set by automated metadata")
    @JsonInclude(Include.NON_NULL)
    private boolean computedGeometry;

    @Schema(description = "Creation date in ISO format")
    private ZonedDateTime createdOn;

    @Schema(description = "Geometry data CRS")
    private String crs;

    @Schema(description = "File encoding")
    private String encoding;

    @Schema(description = "The file format, physical medium, or dimensions of the resource", example = "ESRI Shapefile")
    private String format;

    @Schema(implementation = GeometryAsJson.class, description = "Geometry as GeoJSON")
    private Geometry geometry;

    @Schema(description = "Ingest information")
    private ResourceIngestionDataDto ingestData;

    @Schema(description =
        "Service unique identifier. "
      + "If the service is submitted and a workflow instance "
      + "is initialized, it is used as the business key"
    )
    private UUID key;

    @Schema(description = "Owner details")
    private SimpleAccountDto owner;

    @Schema(description = "Relative path to Topio Drive")
    private String path;

    @Schema(description = "Resource file name")
    private String fileName;

    @Schema(description = "Resource file size")
    private Long fileSize;

    @Schema(description = "Pricing model specified by the Platform")
    private PerCallPricingModelCommandDto pricingModel;

    @JsonInclude(Include.NON_EMPTY)
    private String processDefinition;

    @JsonInclude(Include.NON_EMPTY)
    private String processInstance;

    @Schema(description = "Service type", example = "WMS")
    private EnumUserServiceType serviceType;

    @Schema(description = "Service status")
    private EnumUserServiceStatus status;

    @Schema(description = "Service title")
    private String title;

    @Schema(description = "Date of last update in ISO format")
    private ZonedDateTime updatedOn;

    @Schema(description = "Service version")
    private String version;

    @Hidden
    @Schema(description = "Workflow error details")
    @JsonInclude(Include.NON_EMPTY)
    private String workflowErrorDetails;

    @Hidden
    @ArraySchema(
        arraySchema = @Schema(
            description = "Workflow error messages"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @JsonInclude(Include.NON_EMPTY)
    private List<Message> workflowErrorMessages;

    @JsonInclude(Include.NON_NULL)
    private SimpleHelpdeskAccountDto helpdeskSetErrorAccount;

    @Schema(description = "Helpdesk error message")
    @JsonInclude(Include.NON_EMPTY)
    private String helpdeskErrorMessage;

    @JsonIgnore
    public boolean isDeleted() {
        return this.status == EnumUserServiceStatus.DELETED;
    }
}
