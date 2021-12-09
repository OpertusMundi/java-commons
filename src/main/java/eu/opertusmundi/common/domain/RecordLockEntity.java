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

import eu.opertusmundi.common.model.EnumRecordLock;
import eu.opertusmundi.common.model.RecordLockDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "RecordLock")
@Table(
    schema = "web",
    name = "`record_lock`",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_record_lock_type_id", columnNames = {"`record_type`", "`record_id`"}
    )}
)
public class RecordLockEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.record_lock_id_seq", name = "record_lock_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "record_lock_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account", nullable = false)
    @Getter
    @Setter
    private AccountEntity owner;

    @NotNull
    @Column(name = "`record_type`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumRecordLock recordType;

    @NotNull
    @Column(name = "`record_id`")
    @Getter
    @Setter
    private int recordId;

    @NotNull
    @Column(name = "`granted_on`")
    @Getter
    @Setter
    private ZonedDateTime grantedOn = ZonedDateTime.now();

    public RecordLockDto toDto() {
        final RecordLockDto l = new RecordLockDto();

        l.setId(id);
        l.setOwnerEmail(this.owner.getEmail());
        l.setOwnerId(this.owner.getId());
        l.setOwnerKey(this.owner.getKey());
        l.setRecordId(recordId);
        l.setRecordType(recordType);
        l.setCreatedOn(grantedOn);

        return l;
    }
}
