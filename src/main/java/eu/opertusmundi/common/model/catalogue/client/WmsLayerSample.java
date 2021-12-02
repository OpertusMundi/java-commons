package eu.opertusmundi.common.model.catalogue.client;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class WmsLayerSample {

    private Geometry bbox;

    @JsonInclude(Include.NON_NULL)
    private byte[] image;

    public static WmsLayerSample of(Geometry bbox, byte[] image) {
        final WmsLayerSample s = new WmsLayerSample();
        s.setBbox(bbox);
        s.setImage(image);
        return s;
    }

}
