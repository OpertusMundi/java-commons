package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import eu.opertusmundi.common.model.dto.BankAccountCommandDto;
import eu.opertusmundi.common.model.dto.BankAccountDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BankAccountEmbeddable {

    @Column
    private String id;

    @Column
    @EqualsAndHashCode.Include
    private String ownerName;

    @Embedded
    @EqualsAndHashCode.Include
    private AddressEmbeddable ownerAddress;

    @Column
    @EqualsAndHashCode.Include
    private String iban;

    @Column
    @EqualsAndHashCode.Include
    private String bic;

    @Column
    @EqualsAndHashCode.Include
    private String tag;

    public BankAccountDto toDto() {
        final BankAccountDto a = new BankAccountDto();

        a.setBic(this.bic);
        a.setIban(this.iban);
        a.setId(this.id);
        if (this.ownerAddress != null) {
            a.setOwnerAddress(this.ownerAddress.toDto());
        }
        a.setOwnerName(this.ownerName);
        a.setTag(this.tag);

        return a;
    }

    @Override
    public BankAccountEmbeddable clone() {
        final BankAccountEmbeddable c = new BankAccountEmbeddable();

        c.bic          = this.bic;
        c.iban         = this.iban;
        c.id           = this.id;
        c.ownerAddress = this.ownerAddress.clone();
        c.ownerName    = this.ownerName;
        c.tag          = this.tag;

        return c;
    }

    public static BankAccountEmbeddable from(BankAccountCommandDto b) {
        final BankAccountEmbeddable e = new BankAccountEmbeddable();

        e.bic          = b.getBic();
        e.iban         = b.getIban();
        e.id           = b.getId();
        e.ownerAddress = AddressEmbeddable.from(b.getOwnerAddress());
        e.ownerName    = b.getOwnerName();
        e.tag          = b.getTag();

        return e;
    }

}
