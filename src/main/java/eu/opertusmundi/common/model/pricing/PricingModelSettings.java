package eu.opertusmundi.common.model.pricing;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.catalogue.client.EnumType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PricingModelSettings {

    @ArraySchema(
        arraySchema = @Schema(
            description = "Resource types to which this pricing model can be applied"
        ),
        minItems = 1,
        uniqueItems = true
    )
    private List<EnumType> applicableTo;

    @JsonIgnore
    private boolean enabled;

    @Schema(description = "`True` if this model exclusive i.e. cannot be listed with other pricing models")
    private boolean exclusive;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Pricing models that are allowed to listed with this model. If `exclusive` is `True` "
                        + "this property is ignored."
        ),
        minItems = 0,
        uniqueItems = true
    )
    private List<EnumPricingModel> exclusiveWith;

    @Schema(description = "The pricing model")
    private EnumPricingModel pricingModel;

}
