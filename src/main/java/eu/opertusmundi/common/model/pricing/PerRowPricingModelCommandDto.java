package eu.opertusmundi.common.model.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import eu.opertusmundi.common.model.payment.ServiceUseStatsDto;
import eu.opertusmundi.common.util.Collections;
import io.jsonwebtoken.lang.Assert;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class PerRowPricingModelCommandDto extends BasePricingModelCommandDto {

    private static final long serialVersionUID = 1L;

    protected PerRowPricingModelCommandDto() {
        super(EnumPricingModel.PER_ROW);
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
        // Pricing model and quotation parameters (if not null) must have the same type
        if (params != null && !(params instanceof PerRowQuotationParametersDto)) {
            throw new QuotationException(QuotationMessageCode.INVALID_PARAMETER_TYPE, String.format(
                "Invalid parameter type [expected=%s, found=%s]", this.getType(), params.getType()
            ));
        }
    }

    @Override
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

        final PerRowQuotationParametersDto typedParams = (PerRowQuotationParametersDto) params;
        final Integer                      tier        = typedParams.getPrePaidTier();

        if (tier != null && (this.prePaidTiers == null || tier < 0 || tier >= this.prePaidTiers.size())) {
            throw new QuotationException(QuotationMessageCode.PRE_PAID_TIER_NOT_FOUND, "Prepaid tier was not found");
        }
    }

    @Override
    public EffectivePricingModelDto compute(QuotationParametersDto userParams, SystemQuotationParametersDto systemParams) {
        this.checkUserParametersType(userParams);

        if (userParams == null) {
            return EffectivePricingModelDto.from(this, new EmptyQuotationParametersDto(), systemParams);
        }

        Assert.isInstanceOf(PerRowQuotationParametersDto.class, userParams);

        final PerRowQuotationParametersDto typedParams = (PerRowQuotationParametersDto) userParams;

        if (typedParams.getPrePaidTier() != null) {
            final PrePaidTierDto tier = this.prePaidTiers.get(typedParams.getPrePaidTier());
            final QuotationDto quotation = new QuotationDto();

            quotation.setTaxPercent(systemParams.getTaxPercent().intValue());
            quotation.setTotalPriceExcludingTax(this.getPrice()
                .multiply(BigDecimal.valueOf(tier.getCount()))
                .multiply(BigDecimal.valueOf(100).subtract(tier.getDiscount()))
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

    @Override
    public QuotationDto compute(ServiceUseStatsDto stats, SystemQuotationParametersDto systemParams) throws QuotationException {
        long rowsLeft = stats.getRows();
        BigDecimal price    = BigDecimal.ZERO;

        if (!Collections.isEmpty(this.discountRates)) {
            for (final DiscountRateDto block : this.discountRates) {
                final long       blockCount = rowsLeft > block.getCount() ? block.getCount() : rowsLeft;
                if (blockCount > 0) {
                    final BigDecimal discount = BigDecimal.valueOf(100).subtract(block.getDiscount());
                    final BigDecimal cost     = BigDecimal.valueOf(blockCount)
                        .multiply(this.price)
                        .multiply(discount)
                        .divide(new BigDecimal(100))
                        .setScale(2, RoundingMode.HALF_UP);
                    price = price.add(cost);
                }
                rowsLeft -= blockCount;
            }
        }
        if (rowsLeft > 0) {
            final BigDecimal cost = BigDecimal.valueOf(rowsLeft).multiply(this.price).setScale(2, RoundingMode.HALF_UP);
            price = price.add(cost);
        }

        final QuotationDto quotation = new QuotationDto();

        quotation.setTaxPercent(systemParams.getTaxPercent().intValue());
        quotation.setTotalPriceExcludingTax(price.setScale(2, RoundingMode.HALF_UP));

        quotation.setTax(quotation.getTotalPriceExcludingTax()
            .multiply(systemParams.getTaxPercent())
            .divide(new BigDecimal(100))
            .setScale(2, RoundingMode.HALF_UP)
        );

        return quotation;
    }
}
