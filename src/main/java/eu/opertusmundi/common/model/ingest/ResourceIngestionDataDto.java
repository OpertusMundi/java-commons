package eu.opertusmundi.common.model.ingest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class ResourceIngestionDataDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "The resource unique identifier")
    private UUID key;

    @Schema(description = "The number of features stored in the table.")
    private long features;

    @Schema(description = "The schema of the created table.")
    private String schema;

    @Schema(description = "The name of the created table. The table name is equal to the resource unique identifier")
    private String tableName;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Service endpoints"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @JsonInclude(Include.NON_EMPTY)
    private List<ServiceEndpoint> endpoints = new ArrayList<>();

    public static ResourceIngestionDataDto from(UUID key, ServerIngestResultResponseDto s) {
        final ResourceIngestionDataDto c = new ResourceIngestionDataDto();
        c.key       = key;
        c.features  = s.getLength();
        c.schema    = s.getSchema();
        c.tableName = s.getTable();
        return c;
    }

    public void addEndpoint(EnumSpatialDataServiceType type, String uri) {
        final ServiceEndpoint current = endpoints.stream().filter(e -> e.type == type).findFirst().orElse(null);

        if (current == null) {
            this.endpoints.add(new ServiceEndpoint(type, uri));
        } else {
            current.setUri(uri);
        }
    }

    public ServiceEndpoint getEndpointByServiceType(EnumSpatialDataServiceType type) {
        return endpoints.stream().filter(e -> e.type == type).findFirst().orElse(null);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class ServiceEndpoint {

        @Schema(description = "Service type")
        private EnumSpatialDataServiceType type;

        @Schema(description = "Service endpoint URI")
        private String uri;

    }

}
