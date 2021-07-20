package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.contract.helpdesk.MasterContractDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Contract")
@Table(
    schema = "contract", name = "`master_contract`"
)
public class MasterContractEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(
        sequenceName = "contract.master_contract_id_seq", name = "master_contract_id_seq", allocationSize = 1
    )
    @GeneratedValue(generator = "master_contract_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id ;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Getter
    @Setter
    private UUID key;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`owner`", nullable = false)
    @Getter
    @Setter
    private HelpdeskAccountEntity owner;

    @NotNull
    @OneToOne(
        optional = true, fetch = FetchType.LAZY, orphanRemoval = false
    )
    @JoinColumn(name = "`parent`")
    @Getter
    @Setter
    private MasterContractHistoryEntity parent;

    @OneToMany(
        mappedBy = "contract", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true
    )
    @Getter
    @Setter
    private List<MasterSectionEntity> sections = new ArrayList<>();

    @Column(name = "`title`")
    @Getter
    @Setter
    private String title;

    @Column(name = "`subtitle`")
    @Getter
    @Setter
    private String subtitle;

    @Column(name = "`version`")
    @Getter
    @Setter
    private String version;

    @Column(name = "`created_at`")
    @Getter
    @Setter
    private ZonedDateTime createdAt;

    @Column(name = "`modified_at`")
    @Getter
    @Setter
    private ZonedDateTime modifiedAt;

    public MasterContractDto toDto(boolean includeDetails) {
        final MasterContractDto c = new MasterContractDto();

        c.setCreatedAt(createdAt);
        c.setId(id);
        c.setKey(key);
        c.setModifiedAt(modifiedAt);
        c.setOwner(owner.toSimpleDto());
        c.setSubtitle(subtitle);
        c.setTitle(title);
        c.setVersion(version);

        if (includeDetails) {
            c.setContractParentId(parent == null ? null : parent.getId());
            c.setContractRootId(parent == null ? null : parent.getContractRoot().getId());

            c.setSections(sections.stream()
                .map(MasterSectionEntity::toDto)
                .collect(Collectors.toList())
            );
        }
        return c;
    }

    public static MasterContractEntity from(MasterContractHistoryEntity h) {
        final MasterContractEntity e = new MasterContractEntity();

        e.setCreatedAt(h.getCreatedAt());
        e.setKey(h.getKey());
        e.setModifiedAt(h.getModifiedAt());
        e.setOwner(h.getOwner());
        e.setParent(h);
        e.setSections(h.getSections().stream().map(MasterSectionEntity::from).collect(Collectors.toList()));
        e.setSubtitle(h.getSubtitle());
        e.setTitle(h.getTitle());
        e.setVersion(h.getVersion());

        e.getSections().forEach(s -> s.setContract(e));

        return e;
    }

}
