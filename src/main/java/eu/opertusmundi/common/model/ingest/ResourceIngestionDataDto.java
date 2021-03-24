package eu.opertusmundi.common.model.ingest;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "The number of features stored in the table.")
    private long features;

    @Schema(description = "The schema of the created table.")
    private String schema;

    @Schema(description = "The name of the created table.")
    private String tableName;

    public static ResourceIngestionDataDto from(ServerIngestResultResponseDto s) {
        final ResourceIngestionDataDto c = new ResourceIngestionDataDto();
        c.features  = s.getLength();
        c.schema    = s.getSchema();
        c.tableName = s.getTable();
        return c;
    }

}
