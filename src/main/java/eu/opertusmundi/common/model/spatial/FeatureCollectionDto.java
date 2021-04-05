package eu.opertusmundi.common.model.spatial;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeatureCollectionDto<F extends Feature> {

    public FeatureCollectionDto() {
        this.type = "FeatureCollection";
    }

    @Schema(description = "Object type. Always equal to `FeatureCollection`")
    @Getter
    private String type;


    @Schema(description = "Array of features")
    @Getter
    @Setter
    private List<F> features;

    public static <F extends Feature> FeatureCollectionDto<F> of(List<F> features) {
        final FeatureCollectionDto<F> r = new FeatureCollectionDto<F>();
        r.setFeatures(features);
        return r;
    }

}
