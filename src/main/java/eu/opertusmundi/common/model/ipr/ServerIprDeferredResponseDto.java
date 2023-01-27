package eu.opertusmundi.common.model.ipr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServerIprDeferredResponseDto {

    @Schema(description = "The URI to poll for the status of the request")
    private String statusUri;

    @Schema(description = "The unique ticket assigned to the request")
    private String ticket;

    @Schema(description = "Request type")
    private String type;

}
