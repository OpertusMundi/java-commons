package eu.opertusmundi.common.model.pricing;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class FixedPopulationQuotationParametersDto extends QuotationParametersDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected FixedPopulationQuotationParametersDto() {
        super(EnumPricingModel.FIXED_FOR_POPULATION);
    }

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
    @Getter
    @Setter
    @NotEmpty
    private List<String> nuts;

}
