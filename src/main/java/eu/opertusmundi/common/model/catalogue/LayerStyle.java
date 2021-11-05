package eu.opertusmundi.common.model.catalogue;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "WMS layer style")
public class LayerStyle implements Serializable {

    private static final long serialVersionUID = 1L;

    public LayerStyle(LayerStyle s) {
        this.abstractText = s.getAbstractText();
        this.legendUrls   = s.getLegendUrls();
        this.name         = s.getName();
        this.title        = s.getTitle();
    }

    @JsonProperty("abstract")
    private String abstractText;

    private List<LegendUrl> legendUrls;

    private String name;

    private String title;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    @Schema(description = "WMS layer style legend URL")
    public static class LegendUrl implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "A Base64 encoded example image of the style")
        private byte[] image;

        private String url;
    }
}