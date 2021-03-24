package eu.opertusmundi.common.model.pricing;

import java.util.UUID;

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
    private String assetId;

    @Schema(description = "Pricing model unique key")
    private UUID pricingModelKey;

    @Schema(description = "Quotation parameters")
    private QuotationParametersDto parameters;

}
