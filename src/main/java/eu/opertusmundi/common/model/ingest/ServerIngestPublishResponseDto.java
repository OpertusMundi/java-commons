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

    @Schema(description = "")
    private String wmsBase;

    @Schema(description = "The WMS URL for a DescribeLayer request")
    private String wmsDescribeLayer;

    @Schema(description =
        "An example WMS URL for a GetMap request. Note that this is not a valid request as it lacks several "
      + "required query parameters (as bbox, width and height)"
    )
    private String wmsGetMap;

    @Schema(description = "The WFS endpoint")
    private String wfsBase;

    @Schema(description = "The WFS URL for a DescribeFeatureType request")
    private String wfsDescribeFeatureType;

    @Schema(description = "The WFS URL for a GetFeature request for all contained features (records)")
    private String wfsGetFeature;

}
