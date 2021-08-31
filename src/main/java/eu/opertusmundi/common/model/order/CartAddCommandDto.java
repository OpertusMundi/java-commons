package eu.opertusmundi.common.model.order;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CartAddCommandDto {

    /**
     * Cart key injected by the controller
     */
    @JsonIgnore
    private UUID cartKey;

    @Schema(description = "Catalogue asset PID", required = true)
    @NotEmpty
    private String assetId;

    @Schema(description = "Pricing model unique key", required = true)
    @NotNull
    private UUID pricingModelKey;

    @Schema(description = "Quotation parameters")
    @NotNull
    @Valid
    private QuotationParametersDto parameters;

    /**
     * Quotation computed by the server
     */
    @JsonIgnore
    private EffectivePricingModelDto quotation;

}
