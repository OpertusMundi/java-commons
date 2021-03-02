package eu.opertusmundi.common.model.ingest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServerIngestDeferredResponseDto {

    @Schema(description = "The status endpoint to poll for the status of the request.")
    private String status;

    @Schema(description = "The ticket corresponding to the request.")
    private String ticket;

    @Schema(description = "The response type as requested.")
    private String type;

}
