package eu.opertusmundi.common.model.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mangopay.entities.BankAccount;
import com.mangopay.entities.subentities.BankAccountDetailsIBAN;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountDto extends BankAccountBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private AddressDto ownerAddress;

    @JsonIgnore
    private String tag;

    public static BankAccountDto from(BankAccount b) {
        final BankAccountDetailsIBAN details = (BankAccountDetailsIBAN) b.getDetails();
        final BankAccountDto         result  = new BankAccountDto();

        result.setBic(details.getBic());
        result.setIban(details.getIban());
        result.setOwnerAddress(AddressDto.from(b.getOwnerAddress()));
        result.setOwnerName(b.getOwnerName());

        return result;
    }

}
