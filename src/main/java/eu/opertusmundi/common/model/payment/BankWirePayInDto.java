package eu.opertusmundi.common.model.payment;

import eu.opertusmundi.common.model.dto.BankAccountDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A Bank wire PayIn
 */
@NoArgsConstructor
@Getter
@Setter
public class BankWirePayInDto extends PayInDto {

    @Schema(description = "The user has to proceed a Bank wire with this reference")
    private String wireReference;

    @Schema(description = "The user has to proceed a Bank wire to this bank account")
    private BankAccountDto bankAccount;
}
