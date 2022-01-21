package eu.opertusmundi.common.model.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.collections4.CollectionUtils;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class FixedPopulationPricingModelCommandDto extends BasePricingModelCommandDto {

    private static final long serialVersionUID = 1L;

    public FixedPopulationPricingModelCommandDto() {
        super(EnumPricingModel.FIXED_FOR_POPULATION);
    }

    @Schema(description = "The price prospective clients will pay per 10,000 people")
    @NotNull
    @DecimalMin(value = "0.000", inclusive = false)
    @Digits(integer = 3, fraction = 3)
    @Getter
    @Setter
    private BigDecimal price;

    @Schema(description = "The minimum population percentage of the entire asset clients can purchase per transaction")
    @NotNull
    @Min(1)
    @Max(100)
    @Getter
    @Setter
    private Integer minPercent;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Discount rates based on the the size of population in tens of thousands of people. "
                        + "Each element (except for the first one) must have a `count` property with a value "
                        + "greater than the previous one"
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
        // Pricing model and quotation parameters (if not null) must have the same type
        if (params != null && !(params instanceof FixedPopulationQuotationParametersDto)) {
            throw new QuotationException(QuotationMessageCode.INVALID_PARAMETER_TYPE, String.format(
                "Invalid parameter type [expected=%s, found=%s]", this.getType(), params.getType()
            ));
        }
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

        final FixedPopulationQuotationParametersDto typedParams = (FixedPopulationQuotationParametersDto) params;

        if (!ignoreMissing && CollectionUtils.isEmpty(typedParams.getNuts())) {
            throw new QuotationException(QuotationMessageCode.NO_NUTS_SELECTED, "At least a region must be selected");
        }
    }

    @Override
    public EffectivePricingModelDto compute(QuotationParametersDto userParams, SystemQuotationParametersDto systemParams) {
        this.checkUserParametersType(userParams);

        if (systemParams != null && systemParams.getPopulation() != null) {
            final QuotationDto quotation = new QuotationDto();
            BigDecimal         discount  = BigDecimal.ZERO;

            quotation.setTaxPercent(systemParams.getTaxPercent().intValue());

            if (this.discountRates != null) {
                for (final DiscountRateDto r : this.discountRates) {
                    if (systemParams.getPopulation() > r.getCount()) {
                        discount = r.getDiscount();
                    }
                }
            }
            quotation.setTotalPriceExcludingTax(this.getPrice()
                .multiply(BigDecimal.valueOf(systemParams.getPopulation()))
                .multiply(BigDecimal.valueOf(100).subtract(discount))
                .divide(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP)
            );

            quotation.setTax(quotation.getTotalPriceExcludingTax()
                .multiply(systemParams.getTaxPercent())
                .divide(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP)
            );

            return EffectivePricingModelDto.from(this, userParams, systemParams, quotation);
        }

        return EffectivePricingModelDto.from(this, userParams, systemParams);
    }

}
