package eu.opertusmundi.common.model.ingest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import io.swagger.v3.oas.annotations.Hidden;
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
    private String key;

    @Schema(description = "The number of features stored in the table.")
    private long features;

    @Hidden
    @Schema(description = "The schema of the created table.")
    @JsonInclude(Include.NON_EMPTY)
    private String schema;

    @Hidden
    @Schema(description = "The name of the created table. The table name is equal to the resource unique identifier")
    @JsonInclude(Include.NON_EMPTY)
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

    public static ResourceIngestionDataDto from(String key, ServerIngestResultResponseDto s) {
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
            this.endpoints = new ArrayList<>(this.endpoints);
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
    public static class ServiceEndpoint implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "Service type")
        private EnumSpatialDataServiceType type;

        @Schema(description = "Service endpoint URI")
        private String uri;

    }

}
