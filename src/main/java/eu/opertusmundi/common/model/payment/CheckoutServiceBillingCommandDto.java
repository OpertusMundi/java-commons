package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutServiceBillingCommandDto {

    @JsonIgnore
    private UUID userKey;

    @JsonIgnore
    private BigDecimal totalPrice;

    @JsonIgnore
    private BigDecimal totalPriceExcludingTax;

    @JsonIgnore
    private BigDecimal totalTax;

    @Schema(description = "The subscription billing record keys to include in the PayIn")
    @ArraySchema(minItems = 1)
    private List<UUID> keys;
}
