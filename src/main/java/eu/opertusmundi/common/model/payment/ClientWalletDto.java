package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mangopay.entities.Wallet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientWalletDto {

    @JsonIgnore
    private String id;

    private String currency;

    private BigDecimal amount;

    private EnumFundsType fundsType;

    public static ClientWalletDto from(Wallet w) {
        final ClientWalletDto o = new ClientWalletDto();

        o.setAmount(BigDecimal.valueOf(w.getBalance().getAmount()).divide(BigDecimal.valueOf(100L)));
        o.setCurrency(w.getBalance().getCurrency().toString());
        o.setFundsType(EnumFundsType.from(w.getFundsType()));
        o.setId(w.getId());

        return o;
    }

}