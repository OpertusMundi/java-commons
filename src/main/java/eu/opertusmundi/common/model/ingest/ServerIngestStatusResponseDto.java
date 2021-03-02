package eu.opertusmundi.common.model.ingest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServerIngestStatusResponseDto {

    @Schema(description = "If ingestion/publication has failed,a short comment describing the reason.")
    private String comment;

    @Schema(description = "Whether ingestion/publication process has been completed or not.")
    private boolean completed;

    @Schema(description = "The execution time in seconds.")
    private String executionTime;

    @Schema(description = "The timestamp of the request")
    private String requested;

    @Schema(description = "Whether the process completed succesfully.")
    private boolean success;

}
