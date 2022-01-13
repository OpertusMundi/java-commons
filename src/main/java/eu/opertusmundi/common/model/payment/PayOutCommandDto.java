package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.util.UUID;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PayOutCommandDto {

    @JsonIgnore
    private UUID adminUserKey;

    @JsonIgnore
    private UUID providerKey;

    @Schema(description = "Information about the funds that are being debited")
    @Digits(integer = 10, fraction = 2)
    @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal debitedFunds;

    @JsonIgnore
    private BigDecimal fees;

}
