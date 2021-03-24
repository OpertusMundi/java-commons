package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TransferCommandDto {

    private UUID paymentKey;
    
    private UUID debitedUserKey;

    private UUID creditedUserKey;

    private int debitedFunds;
    
    private int fees;

}
