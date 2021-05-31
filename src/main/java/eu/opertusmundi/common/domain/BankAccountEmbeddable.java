package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;

import eu.opertusmundi.common.model.account.BankAccountCommandDto;
import eu.opertusmundi.common.model.account.BankAccountDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@MappedSuperclass
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BankAccountEmbeddable {

    @Column
    @EqualsAndHashCode.Include
    protected String ownerName;

    @Embedded
    @EqualsAndHashCode.Include
    protected AddressEmbeddable ownerAddress;

    @Column
    @EqualsAndHashCode.Include
    protected String iban;

    @Column
    @EqualsAndHashCode.Include
    protected String bic;

    public BankAccountDto toDto() {
        final BankAccountDto a = new BankAccountDto();

        a.setBic(this.bic);
        a.setIban(this.iban);
        if (this.ownerAddress != null) {
            a.setOwnerAddress(this.ownerAddress.toDto());
        }
        a.setOwnerName(this.ownerName);

        return a;
    }

    @Override
    public BankAccountEmbeddable clone() {
        final BankAccountEmbeddable c = new BankAccountEmbeddable();

        c.bic          = this.bic;
        c.iban         = this.iban;
        c.ownerAddress = this.ownerAddress.clone();
        c.ownerName    = this.ownerName;

        return c;
    }

    public static BankAccountEmbeddable from(BankAccountCommandDto b) {
        final BankAccountEmbeddable e = new BankAccountEmbeddable();

        e.bic          = b.getBic();
        e.iban         = b.getIban();
        e.ownerAddress = AddressEmbeddable.from(b.getOwnerAddress());
        e.ownerName    = b.getOwnerName();

        return e;
    }

    public static BankAccountEmbeddable from(BankAccountDto b) {
        final BankAccountEmbeddable e = new BankAccountEmbeddable();

        e.bic          = b.getBic();
        e.iban         = b.getIban();
        e.ownerAddress = AddressEmbeddable.from(b.getOwnerAddress());
        e.ownerName    = b.getOwnerName();

        return e;
    }
    
}
