package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class CatalogueItemStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    public CatalogueItemStatistics() {
        this.downloads = 0;
        this.sales     = 0;
        this.rating    = null;
    }

    @JsonIgnore
    private String pid;

    @Schema(description = "Total number of downloads", example = "100")
    private int downloads;

    @Schema(description = "Total number of orders", example = "10")
    private int sales;

    @Schema(description = "Average rating. If no user ratings exist, null is returned", example = "4.5", minimum = "0", maximum = "5")
    private BigDecimal rating;

}
