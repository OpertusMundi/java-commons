package eu.opertusmundi.common.model.sinergise;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeatureDto {

    private String                id;
    private Geometry              geometry;
    private BigDecimal[]          bbox;
    private JsonNode              properties;
    private List<LinkDto>         links;
    private Map<String, AssetDto> assets;

    @JsonIgnore
    public String getThumbnail() {
        if (CollectionUtils.isEmpty(assets)) {
            return null;
        }
        return this.assets.values().stream()
            .filter(a -> a.getTitle().equalsIgnoreCase("thumbnail"))
            .map(a -> a.getHref())
            .findFirst()
            .orElse(null);
    }
}
