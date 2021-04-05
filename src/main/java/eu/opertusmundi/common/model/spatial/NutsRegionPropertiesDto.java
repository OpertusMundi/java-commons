package eu.opertusmundi.common.model.spatial;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NutsRegionPropertiesDto {

    @Schema(description = "Region unique code (NUTS identifier)")
    protected String code;

    @Schema(description = "Region level (NUTS level)")
    protected Long level;

    @Schema(description = "Region latin name")
    protected String nameLatin;
    
    @Schema(description = "Region name")
    protected String name;

    @Schema(description = "Region population")
    protected Long population;

}
