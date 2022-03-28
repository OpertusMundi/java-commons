package eu.opertusmundi.common.model.pricing;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class QuotationCommandDto {

    @Schema(description = "Asset unique PID")
    @NotBlank
    private String assetId;

    @Schema(description = "Pricing model unique key")
    @NotNull
    private String pricingModelKey;

    @Schema(description = "Quotation parameters")
    @NotNull
    @Valid
    private QuotationParametersDto parameters;

}
