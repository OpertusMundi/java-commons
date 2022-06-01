package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

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

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.account.AccountClientDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AccountClient")
@Table(schema = "web", name = "`account_client`")
public class AccountClientEntity {

    protected AccountClientEntity() {
    }

    public AccountClientEntity(String alias, UUID clientId) {
        this.alias = alias;
        this.clientId = clientId;
    }

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.account_client_id_seq", name = "account_client_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_client_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "account", nullable = false)
    @Getter
    @Setter
    private AccountEntity account;

    @NotNull
    @Column(name = "`alias`")
    @Getter
    private String alias;

    @NotNull
    @NaturalId
    @Column(name = "client_id", updatable = false, columnDefinition = "uuid")
    @Getter
    private UUID clientId;

    @NotNull
    @Column(name = "`created_on`")
    @Getter
    private final ZonedDateTime createdOn = ZonedDateTime.now();

    @Column(name = "`revoked_on`")
    @Getter
    private ZonedDateTime revokedOn;

    public void revoke() {
        if (this.revokedOn == null) {
            this.revokedOn = ZonedDateTime.now();
        }
    }

    public AccountClientDto toDto() {
        final AccountClientDto c = new AccountClientDto();

        c.setAlias(alias);
        c.setCreatedOn(createdOn);
        c.setId(id);
        c.setClientId(clientId);
        c.setRevokedOn(revokedOn);

        return c;
    }
}
