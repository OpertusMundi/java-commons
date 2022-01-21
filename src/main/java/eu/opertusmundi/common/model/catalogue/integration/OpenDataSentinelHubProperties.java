package eu.opertusmundi.common.model.catalogue.integration;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenDataSentinelHubProperties extends SentinelHubProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    public OpenDataSentinelHubProperties() {
        super(SentinelHubProperties.EnumType.OPEN_DATA);
    }

    @Schema(description = "`true` if this is a open data collection")
    private boolean open;

    @Schema(description = "Open data collection name", example = "sentinel-1-grd")
    @NotEmpty
    private String collection;

}
