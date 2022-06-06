package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import eu.opertusmundi.common.model.account.BankAccountCommandDto;
import eu.opertusmundi.common.model.account.BankAccountDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class CustomerBankAccountEmbeddable extends BankAccountEmbeddable implements Cloneable {

    @Column
    @EqualsAndHashCode.Include
    private String id;

    @Column
    @EqualsAndHashCode.Include
    private String tag;

    public BankAccountDto toDto() {
        return this.toDto(false);
    }

    public BankAccountDto toDto(boolean includeHelpdeskData) {
        final BankAccountDto a = new BankAccountDto();

        a.setBic(this.bic);
        a.setIban(this.iban);
        if (includeHelpdeskData) {
            a.setId(this.id);
        }
        if (this.ownerAddress != null) {
            a.setOwnerAddress(this.ownerAddress.toDto());
        }
        a.setOwnerName(this.ownerName);
        a.setTag(this.tag);

        return a;
    }

    @Override
    public CustomerBankAccountEmbeddable clone() {
        final CustomerBankAccountEmbeddable c = new CustomerBankAccountEmbeddable();

        c.bic          = this.bic;
        c.iban         = this.iban;
        c.id           = this.id;
        c.ownerAddress = this.ownerAddress.clone();
        c.ownerName    = this.ownerName;
        c.tag          = this.tag;

        return c;
    }

    public static CustomerBankAccountEmbeddable from(BankAccountCommandDto b) {
        final CustomerBankAccountEmbeddable e = new CustomerBankAccountEmbeddable();

        e.bic          = b.getBic();
        e.iban         = b.getIban();
        e.id           = b.getId();
        e.ownerAddress = AddressEmbeddable.from(b.getOwnerAddress());
        e.ownerName    = b.getOwnerName();
        e.tag          = b.getTag();

        return e;
    }

}
