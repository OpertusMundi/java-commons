package eu.opertusmundi.common.model.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.ArrayUtils;

import eu.opertusmundi.common.model.payment.ServiceUseStatsDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class FixedRowPricingModelCommandDto extends BasePricingModelCommandDto {

    private static final long serialVersionUID = 1L;

    private static int ROWS_STEP = 1000;

    public FixedRowPricingModelCommandDto() {
        super(EnumPricingModel.FIXED_PER_ROWS);
    }

    @Schema(description = "The price prospective clients will pay per 1,000 rows")
    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 6, fraction = 2)
    @Getter
    @Setter
    private BigDecimal price;

    @Schema(description = "The minimum number of rows a client can purchase per transaction")
    @NotNull
    @Getter
    @Setter
    private Integer minRows;

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
        if (params != null && !(params instanceof FixedRowQuotationParametersDto)) {
            throw new QuotationException(QuotationMessageCode.INVALID_PARAMETER_TYPE, String.format(
                "Invalid parameter type [expected=%s, found=%s]", this.getType(), params.getType()
            ));
        }
    }

    @Override
    public void validate() throws QuotationException {
        // No validation is required
    }

    @Override
    public void validate(QuotationParametersDto params, boolean ignoreMissing) throws QuotationException {
        this.checkUserParametersType(params);

        final FixedRowQuotationParametersDto typedParams = (FixedRowQuotationParametersDto) params;

        if (ArrayUtils.isEmpty(typedParams.getNuts())) {
            throw new QuotationException(QuotationMessageCode.NO_NUTS_SELECTED, "At least a region must be selected");
        }
    }

    @Override
    public EffectivePricingModelDto compute(QuotationParametersDto userParams, SystemQuotationParametersDto systemParams) {
        this.checkUserParametersType(userParams);

        if (systemParams != null && systemParams.getSelectedRows() != null) {
            final QuotationDto quotation = new QuotationDto();
            BigDecimal         discount  = BigDecimal.ZERO;

            quotation.setTaxPercent(systemParams.getTaxPercent().intValue());

            if (this.discountRates != null) {
                for (final DiscountRateDto r : this.discountRates) {
                    if (systemParams.getSelectedRows() > r.getCount()) {
                        discount = r.getDiscount();
                    }
                }
            }
            quotation.setTotalPriceExcludingTax(this.getPrice()
                .multiply(BigDecimal.valueOf(systemParams.getSelectedRows()))
                .divide(new BigDecimal(ROWS_STEP))
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

    @Override
    public QuotationDto compute(ServiceUseStatsDto stats, SystemQuotationParametersDto systemParams) throws QuotationException {
        throw new QuotationException(QuotationMessageCode.QUOTATION_NOT_SUPPORTED, "Model does not support service statistics parameters");
    }


}
