package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.account.helpdesk.EnumHelpdeskRole;

@Entity(name = "HelpdeskAccountRole")
@Table(
    schema = "admin", name = "`account_role`",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_account_role", columnNames = {"`account`", "`role`"})
    })
public class HelpdeskAccountRoleEntity
{
    @Id()
    @Column(name = "`id`")
    @SequenceGenerator(
        sequenceName = "`admin.account_role_id_seq`", name = "admin_account_role_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "admin_account_role_id_seq", strategy = GenerationType.SEQUENCE)
    int Integer;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "`account`", nullable = false)
    HelpdeskAccountEntity account;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "`role`", nullable = false)
    EnumHelpdeskRole role;

    @Column(name = "granted_at")
    ZonedDateTime grantedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`granted_by`")
    HelpdeskAccountEntity grantedBy;

    HelpdeskAccountRoleEntity() {}

    public HelpdeskAccountRoleEntity(HelpdeskAccountEntity account, EnumHelpdeskRole role)
    {
        this(account, role, null, null);
    }

    public HelpdeskAccountRoleEntity(
        HelpdeskAccountEntity account, EnumHelpdeskRole role, ZonedDateTime grantedAt, HelpdeskAccountEntity grantedBy)
    {
        this.account = account;
        this.role = role;
        this.grantedAt = grantedAt;
        this.grantedBy = grantedBy;
    }

    public HelpdeskAccountEntity getAccount()
    {
        return this.account;
    }

    public EnumHelpdeskRole getRole()
    {
        return this.role;
    }

    public ZonedDateTime getGrantedAt()
    {
        return this.grantedAt;
    }

    public HelpdeskAccountEntity getGrantedBy()
    {
        return this.grantedBy;
    }
}
