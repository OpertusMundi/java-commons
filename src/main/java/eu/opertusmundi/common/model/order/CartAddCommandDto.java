package eu.opertusmundi.common.model.order;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.pricing.CallPrePaidQuotationParametersDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.EmptyQuotationParametersDto;
import eu.opertusmundi.common.model.pricing.FixedPopulationQuotationParametersDto;
import eu.opertusmundi.common.model.pricing.FixedRowQuotationParametersDto;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;
import eu.opertusmundi.common.model.pricing.RowPrePaidQuotationParametersDto;
import eu.opertusmundi.common.model.pricing.integration.SHImageQuotationParametersDto;
import eu.opertusmundi.common.model.pricing.integration.SHSubscriptionQuotationParametersDto;
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
    @NotBlank
    private String assetId;

    @Schema(description = "Pricing model unique key", required = true)
    @NotNull
    private String pricingModelKey;

    @Schema(
        description = "Quotation parameters",
        oneOf = {
            EmptyQuotationParametersDto.class,
            CallPrePaidQuotationParametersDto.class,
            FixedRowQuotationParametersDto.class,
            FixedPopulationQuotationParametersDto.class,
            RowPrePaidQuotationParametersDto.class,
            SHImageQuotationParametersDto.class,
            SHSubscriptionQuotationParametersDto.class,
        }
    )
    @NotNull
    @Valid
    private QuotationParametersDto parameters;

    /**
     * Quotation computed by the server
     */
    @JsonIgnore
    private EffectivePricingModelDto quotation;

}
