package eu.opertusmundi.common.model.catalogue;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryMethodOptions implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Type of physical media. Applicable only when delivery method is `PHYSICAL_PROVIDER`.", required = true)
    @JsonInclude(Include.NON_EMPTY)
    private String mediaType;

    @Schema(description = "Notes for buyer")
    @JsonInclude(Include.NON_EMPTY)
    private String notes;

    @Schema(description = "Number of objects. Applicable only when delivery method is `PHYSICAL_PROVIDER`", minimum = "1", required = true)
    @JsonInclude(Include.NON_NULL)
    private Integer numberOfItems;

}
