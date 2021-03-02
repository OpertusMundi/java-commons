package eu.opertusmundi.common.model.ingest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class ServerIngestResultResponseDto {

    @Schema(description = "The number of features stored in the table.")
    private int length;

    @Schema(description = "The schema of the created table.")
    private String schema;

    @Schema(description = "The name of the created table.")
    private String table;

}
