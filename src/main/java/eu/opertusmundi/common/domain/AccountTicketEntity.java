package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.TypeDef;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import eu.opertusmundi.common.model.account.AccountTicketDto;
import eu.opertusmundi.common.model.account.EnumTicketStatus;
import eu.opertusmundi.common.model.account.EnumTicketType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AccountTicket")
@Table(schema = "web", name = "`account_ticket`")
@TypeDef(
    typeClass      = JsonBinaryType.class,
    defaultForType = JsonNode.class
)
@Getter
@Setter
public class AccountTicketEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.account_ticket_id_seq", name = "account_ticket_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_ticket_id_seq", strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PRIVATE)
    private Integer id;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    private UUID key = UUID.randomUUID();

    @NotNull
    @Column(name = "`type`")
    @Enumerated(EnumType.STRING)
    private EnumTicketType type;

    @Column(name = "resource_key", updatable = false, columnDefinition = "uuid")
    private UUID resourceKey;

    @NotNull
    @Column(name = "`status`")
    @Enumerated(EnumType.STRING)
    private EnumTicketStatus status;

    @NotBlank
    @Column(name = "`subject`")
    private String subject;

    @NotBlank
    @Column(name = "`message`")
    private String message;

    @Column(name = "message_thread_key", columnDefinition = "uuid")
    private UUID messageThreadKey;

    @NotNull
    @Column(name = "`created_at`")
    private ZonedDateTime createdAt;

    @NotNull
    @Column(name = "`updated_at`")
    private ZonedDateTime updatedAt;

    @Column(name = "`assigned_at`")
    private ZonedDateTime assignedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner", nullable = false)
    private AccountEntity owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee", nullable = false)
    private HelpdeskAccountEntity assignee;

    public AccountTicketDto toDto() {
        return this.toDto(false);
    }

    public AccountTicketDto toDto(boolean includeHelpdeskDetails) {
        final AccountTicketDto t = new AccountTicketDto();

        t.setCreatedAt(createdAt);
        t.setId(id);
        t.setKey(key);
        t.setMessage(message);
        t.setMessageThreadKey(messageThreadKey);
        t.setResourceKey(resourceKey);
        t.setStatus(status);
        t.setSubject(subject);
        t.setType(type);
        t.setUpdatedAt(updatedAt);

        if (includeHelpdeskDetails) {
            t.setAssignedAt(assignedAt);
            if (this.getAssignee() != null) {
                t.setAssignee(this.getAssignee().toSimpleDto());
            }
            if (this.getOwner() != null) {
                t.setOwner(this.getOwner().toSimpleDto());
            }
        }

        return t;
    }

}
