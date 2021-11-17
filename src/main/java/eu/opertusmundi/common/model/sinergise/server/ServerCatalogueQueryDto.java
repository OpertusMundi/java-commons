package eu.opertusmundi.common.model.sinergise.server;

import java.math.BigDecimal;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.sinergise.FieldsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Sentinel Hub catalogue query
 *
 * @see https://docs.sentinel-hub.com/api/latest/reference/#operation/postSearchSTAC
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ServerCatalogueQueryDto {

    @JsonInclude(Include.NON_NULL)
    private BigDecimal[] bbox;

    @JsonInclude(Include.NON_EMPTY)
    private String[] collections;

    @JsonInclude(Include.NON_EMPTY)
    private String datetime;

    @JsonInclude(Include.NON_EMPTY)
    private String distinct;

    @JsonInclude(Include.NON_NULL)
    private FieldsDto fields;

    @JsonInclude(Include.NON_EMPTY)
    private String[] ids;

    @JsonInclude(Include.NON_NULL)
    private Geometry intersects;

    private int limit;

    @JsonInclude(Include.NON_NULL)
    private Integer next;

    private JsonNode query;

}
