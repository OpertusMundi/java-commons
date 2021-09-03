package eu.opertusmundi.common.model.pricing;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Schema(description = "User-defined quotation parameters")
@Getter
@Setter
@ToString
public class QuotationParametersCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ArraySchema(
        arraySchema = @Schema(
            description = "User-defined parameter of array of NUTS codes. The codes are used for computing asset coverage and population"
        ),
        minItems = 0,
        uniqueItems = true,
        schema = @Schema(
            description = "NUTS codes",
            externalDocs = @ExternalDocumentation(url = "https://ec.europa.eu/eurostat/web/regions-and-cities/overview")
        )
    )
    protected List<String> nuts;

    @Schema(description = "Selected prepaid tier index if feature is supported. If a tier is selected and the pricing "
                        + "model does not support prepaid tiers, quotation service will return a validation error")
    protected Integer prePaidTier;

}
