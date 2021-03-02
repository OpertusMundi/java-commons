package eu.opertusmundi.common.model.ingest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServerIngestTicketResponseDto {

    @Schema(description = "The request of this ticket.", allowableValues = {"ingest", "publish"})
    private String request;

    @Schema(description = "The associated ticket.")
    private String ticket;

}
