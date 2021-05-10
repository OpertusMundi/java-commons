package eu.opertusmundi.common.model.pricing;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.opertusmundi.common.support.BigDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class QuotationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Tax percent", minimum = "0", maximum = "100")
    @Getter
    @Setter
    private int taxPercent;

    @Schema(description = "Price excluding tax")
    @Getter
    @Setter
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal totalPriceExcludingTax;

    @Schema(description = "Price tax")
    @Getter
    @Setter
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal tax;

    @Setter(AccessLevel.PROTECTED)
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal totalPrice;

    @Schema(description = "Price total including tax")
    @JsonProperty("totalPrice")
    public BigDecimal getTotalPrice() {
        return this.totalPriceExcludingTax.add(this.tax);
    }

    @Schema(description = "Platform fees. This field is available only to providers when viewing their published assets")
    @JsonInclude(Include.NON_NULL)
    @Getter
    @Setter
    private BigDecimal fees;

    @Schema(description = "Currency of monetary values", implementation = String.class, example = "EUR")
    @Getter
    @Setter
    private Currency currency = Currency.getInstance("EUR");

}
