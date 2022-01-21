package eu.opertusmundi.common.model.pricing;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class RowBlockRatePricingModelCommandDto extends BasePricingModelCommandDto {

    private static final long serialVersionUID = 1L;

    protected RowBlockRatePricingModelCommandDto() {
        super(EnumPricingModel.PER_ROW_WITH_BLOCK_RATE);
    }

    @Schema(description = "The price per row")
    @DecimalMin(value = "0.000", inclusive = false)
    @Digits(integer = 3, fraction = 3)
    @NotNull
    @Getter
    @Setter
    private BigDecimal price;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Discount rates based on the number of selected rows. Each element (except for the first one) "
                        + "must have a `count` property with a value greater than the previous one"
        ),
        minItems = 0,
        maxItems = 3,
        uniqueItems = true,
        schema = @Schema(description = "Discount rate", implementation = DiscountRateDto.class)
    )
    @Size(min = 0, max = 3)
    @Valid
    @Getter
    @Setter
    private List<DiscountRateDto> discountRates;

    @Override
    protected void checkUserParametersType(QuotationParametersDto params) throws QuotationException {
        // Pricing model does not support user parameters. No action is required
    }

    @Override
    public void validate() throws QuotationException {
        if (this.discountRates != null) {
            for (int i = 1; i < this.discountRates.size(); i++) {
                final DiscountRateDto prev = this.discountRates.get(i - 1);
                final DiscountRateDto curr = this.discountRates.get(i);
                if (prev.getCount() > curr.getCount()) {
                    throw new QuotationException(
                        QuotationMessageCode.DISCOUNT_RATE_COUNT_ORDER,
                        "Value of property count for each discount rate must be in increasing order"
                    );
                }
                if (prev.getDiscount().compareTo(curr.getDiscount()) == 1) {
                    throw new QuotationException(
                        QuotationMessageCode.DISCOUNT_RATE_DISCOUNT_ORDER,
                        "Value of property discount for each discount rate must be in increasing order"
                    );
                }
            }
        }
    }

    @Override
    public void validate(QuotationParametersDto params, boolean ignoreMissing) throws QuotationException {
        this.checkUserParametersType(params);

        // No validation is required
    }

    @Override
    public EffectivePricingModelDto compute(QuotationParametersDto userParams, SystemQuotationParametersDto systemParams) {
        this.checkUserParametersType(userParams);

        final QuotationDto quotation = new QuotationDto();

        quotation.setTaxPercent(systemParams.getTaxPercent().intValue());
        quotation.setTotalPriceExcludingTax(new BigDecimal(0));
        quotation.setTax(new BigDecimal(0));

        return EffectivePricingModelDto.from(this, userParams, systemParams, quotation);
    }

}
