package eu.opertusmundi.common.model.catalogue;

import java.io.Serializable;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryMethodOptions implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Type of physical media")
    @NotEmpty
    private String mediaType;

    @Schema(description = "Notes for buyer")
    private String notes;

    @Schema(description = "Number of objects", minimum = "1")
    @NotNull
    @Min(1)
    private Integer numberOfItems;

}
