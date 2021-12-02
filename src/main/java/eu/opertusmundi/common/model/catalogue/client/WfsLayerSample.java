package eu.opertusmundi.common.model.catalogue.client;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class WfsLayerSample {

    private Geometry bbox;

    private JsonNode data;

    public static WfsLayerSample of(Geometry bbox, JsonNode data) {
        final WfsLayerSample s = new WfsLayerSample();
        s.setBbox(bbox);
        s.setData(data);
        return s;
    }

}
