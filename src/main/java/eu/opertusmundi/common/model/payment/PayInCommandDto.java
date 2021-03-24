package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base PayIn class
 * 
 * Credited user and wallet are derived from the {@link PayInCommandDto#userKey}
 * member.
 */
@NoArgsConstructor
@Getter
@Setter
public abstract class PayInCommandDto {

    @Schema(description = "The declared debited funds. The funds must be equal to the total price of"
                        + "purchased items", required = true)
    private int debitedFunds;

    @JsonIgnore
    private UUID payInKey;

    @JsonIgnore
    private UUID userKey;

}
