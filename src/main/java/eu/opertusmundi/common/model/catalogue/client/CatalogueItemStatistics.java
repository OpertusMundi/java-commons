package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogueItemStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    public CatalogueItemStatistics() {
        this.downloads = 0;
        this.sales     = 0;
        this.rating    = null;
    }

    @Schema(description = "Total number of downloads", example = "100")
    int downloads;

    @Schema(description = "Total number of orders", example = "10")
    int sales;

    @Schema(description = "Average rating. If no user ratings exist, null is returned", example = "4.5", minimum = "0", maximum = "5")
    Integer rating;

}
