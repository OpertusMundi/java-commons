package eu.opertusmundi.common.model.ingest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ServerIngestPublishCommandDto {

    @Schema(description = "Geoserver shard")
    private String shard;

    @Schema(description = "The GeoServer workspace in which the layer will be "
                        + "published (it will be created if does not exist). "
                        + "If not given,the default workspace will be used.")
    private String workspace;

    @Schema(description = "The table name.")
    private String table;

}
