package eu.opertusmundi.common.model.payment;

import java.util.UUID;

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
    private UUID userKey;
    
    @JsonIgnore
    private UUID payOutKey;

    @Schema(description = "A custom reference you wish to appear on the user’s bank statement", maxLength = 12)
    private String bankWireRef;

    @Schema(description = "Information about the funds that are being debited")
    private int debitedFunds;

    @Schema(
        description = "Information about the fees that were taken by the client for this "
                    + "transaction (and were hence transferred to the Client's platform wallet)"
    )
    private int fees;

    @Schema(description = "Application specific information for this PayOut")
    private String tag;

}
