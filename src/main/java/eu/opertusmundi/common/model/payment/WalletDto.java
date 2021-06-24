package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mangopay.entities.Wallet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WalletDto {

    @JsonIgnore
    private String id;

    @JsonIgnore
    private List<String> owners;

    private String currency;

    private BigDecimal amount;

    private EnumFundsType fundsType;

    public static WalletDto from(Wallet w) {
        final WalletDto o = new WalletDto();

        o.setAmount(BigDecimal.valueOf(w.getBalance().getAmount()).divide(BigDecimal.valueOf(100L)));
        o.setCurrency(w.getBalance().getCurrency().toString());
        o.setFundsType(EnumFundsType.from(w.getFundsType()));
        o.setId(w.getId());
        o.setOwners(w.getOwners());

        return o;
    }

}
