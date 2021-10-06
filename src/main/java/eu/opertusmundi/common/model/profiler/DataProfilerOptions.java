package eu.opertusmundi.common.model.profiler;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataProfilerOptions {

    private BigDecimal aspectRatio;

    @Builder.Default
    private String     baseMapName = "Mapnik";

    @Builder.Default
    private String     baseMapProvider = "OpenStreetMap";

    private String     crs;

    private String     encoding;

    private String     geometry;

    private Integer    height;

    private String     lat;

    private String     lon;

    private String     time;

    private Integer    width;

}
