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
public class ServerIngestPublishResponseDto {

    @Schema(description = "WFS endpoint.")
    private String wfs;

    @Schema(description = "WMS endpoint.")
    private String wms;

}
