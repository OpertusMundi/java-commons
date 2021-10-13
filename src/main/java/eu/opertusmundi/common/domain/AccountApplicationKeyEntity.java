package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.account.AccountApplicationKeyDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AccountApplicationKey")
@Table(schema = "web", name = "`account_api_key`")
public class AccountApplicationKeyEntity {

    protected AccountApplicationKeyEntity() {

    }

    public AccountApplicationKeyEntity(String key) {
        this.key = key;
    }

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.account_api_key_id_seq", name = "account_api_key_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_api_key_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "account", nullable = false)
    @Getter
    @Setter
    private AccountEntity account;

    @NotNull
    @Column(name = "`created_on`")
    @Getter
    private final ZonedDateTime createdOn = ZonedDateTime.now();

    @Column(name = "`revoked_on`")
    @Getter
    private ZonedDateTime revokedOn;

    @NotNull
    @Column(name = "`key`")
    @Getter
    private String key;

    public void revoke() {
        if (this.revokedOn != null) {
            this.revokedOn = ZonedDateTime.now();
        }
    }

    public AccountApplicationKeyDto toDto() {
        final AccountApplicationKeyDto k = new AccountApplicationKeyDto();

        k.setCreatedOn(createdOn);
        k.setId(id);
        k.setKey(key);
        k.setRevokedOn(revokedOn);

        return k;
    }

}
