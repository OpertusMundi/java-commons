package eu.opertusmundi.common.model.ingest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServerIngestPromptResponseDto {

    @Schema(description = "The number of features stored in the table.")
    private int length;

    @Schema(description = "The schema of the created table.")
    private String schema;

    @Schema(description = "The name of the created table.")
    private String table;

    @Schema(description = "The response type as requested.")
    private String type;

}
