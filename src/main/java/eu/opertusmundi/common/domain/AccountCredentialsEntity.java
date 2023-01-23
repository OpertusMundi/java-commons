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

import eu.opertusmundi.common.model.account.AccountCredentialsDto;
import lombok.Getter;

@Entity(name = "AccountCredentials")
@Table(schema = "web", name = "`account_credentials`")
@Getter
public class AccountCredentialsEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.account_credentials_id_seq", name = "account_credentials_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_credentials_id_seq", strategy = GenerationType.SEQUENCE)
    private Integer id;

    @NotNull
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "account", nullable = false)
    private AccountEntity account;

    @NotNull
    @Column(name = "`application`", length = 64)
    private String application;

    @NotNull
    @Column(name = "`username`", length = 64)
    private String username;

    @NotNull
    @Column(name = "`password`", length = 64)
    private String password;

    @NotNull
    @Column(name = "`created_on`")
    private final ZonedDateTime createdOn = ZonedDateTime.now();

    public AccountCredentialsDto toDto() {
        return AccountCredentialsDto.of(username, password);
    }
}
