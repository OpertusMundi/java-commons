package eu.opertusmundi.common.model.payment.consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.account.BankAccountDto;
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
public class ConsumerBankwirePayInDto extends ConsumerPayInDto {

    @Schema(description = "The user has to proceed a Bank wire with this reference")
    private String wireReference;

    @Schema(description = "The user has to proceed a Bank wire to this bank account")
    @JsonInclude(Include.NON_NULL)
    private BankAccountDto bankAccount;
}
