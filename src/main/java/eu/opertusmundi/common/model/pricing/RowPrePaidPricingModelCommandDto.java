package eu.opertusmundi.common.model.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

public class RowPrePaidPricingModelCommandDto extends BasePricingModelCommandDto {

    private static final long serialVersionUID = 1L;

    public RowPrePaidPricingModelCommandDto() {
        super(EnumPricingModel.PER_ROW_WITH_PREPAID);
    }

    @Schema(description = "The price per row")
    @NotNull
    @DecimalMin(value = "0.000", inclusive = false)
    @Digits(integer = 3, fraction = 3)
    @Getter
    @Setter
    private BigDecimal price;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Prepaid tiers using data rows as units. Each element (except for the first one) "
                        + "must have a `count` property with a value greater than the previous one"
        ),
        minItems = 0,
        maxItems = 3,
        uniqueItems = true,
        schema = @Schema(description = "Prepaid tier", implementation = DiscountRateDto.class)
    )
    @Size(min = 0, max = 3)
    @Valid
    @Getter
    @Setter
    private List<PrePaidTierDto> prePaidTiers;

    public void validate() throws QuotationException {
        if (this.prePaidTiers != null) {
            for (int i = 1; i < this.prePaidTiers.size(); i++) {
                final PrePaidTierDto prev = this.prePaidTiers.get(i - 1);
                final PrePaidTierDto curr = this.prePaidTiers.get(i);
                if (prev.getCount() > curr.getCount()) {
                    throw new QuotationException(
                        QuotationMessageCode.PREPAID_COUNT_ORDER,
                        "Value of property count for each prepaid tier must be in increasing order"
                    );
                }
                if (prev.getDiscount().compareTo(curr.getDiscount()) == 1) {
                    throw new QuotationException(
                        QuotationMessageCode.PREPAID_DISCOUNT_ORDER,
                        "Value of property discount for each prepaid tier must be in increasing order"
                    );
                }
            }
        }
    }

    public void validate(QuotationParametersDto params) throws QuotationException {
        final Integer tier = params.getPrePaidTier();

        if (tier == null) {
            return;
        }

        if (this.prePaidTiers == null || tier < 0 || tier >= this.prePaidTiers.size()) {
            throw new QuotationException(QuotationMessageCode.PRE_PAID_TIER_NOT_FOUND, "Prepaid tier was not found");
        }
    }

    public  EffectivePricingModelDto compute(QuotationParametersDto params) {
        final EffectivePricingModelDto result = EffectivePricingModelDto.from(this, params);

        if (params.getPrePaidTier() != null) {
            final PrePaidTierDto tier = this.prePaidTiers.get(params.getPrePaidTier());
            final QuotationDto quotation = new QuotationDto();

            quotation.setTaxPercent(params.getTaxPercent().intValue());
            quotation.setTotalPriceExcludingTax(this.getPrice()
                .multiply(BigDecimal.valueOf(tier.getCount()))
                .multiply(BigDecimal.valueOf(100).subtract(tier.getDiscount()))
                .divide(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP)
            );

            quotation.setTax(quotation.getTotalPriceExcludingTax()
                .multiply(params.getTaxPercent())
                .divide(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP)
            );

            result.setQuotation(quotation);
        }

        return result;
    }

}
