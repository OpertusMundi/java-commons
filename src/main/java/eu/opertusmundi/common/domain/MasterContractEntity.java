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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Contract")
@Table(
    schema = "contract", name = "`master_contract`"
)
@Getter
@Setter
public class MasterContractEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(
        sequenceName = "contract.master_contract_id_seq", name = "master_contract_id_seq", allocationSize = 1
    )
    @GeneratedValue(generator = "master_contract_id_seq", strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PRIVATE)
    private Integer id;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    private UUID key;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`owner`", nullable = false)
    private HelpdeskAccountEntity owner;

    @NotNull
    @OneToOne(
        optional = false, fetch = FetchType.LAZY
    )
    @JoinColumn(name = "`parent`")
    private MasterContractHistoryEntity parent;

    @OneToMany(
        mappedBy = "contract", fetch = FetchType.EAGER, cascade = CascadeType.ALL
    )
    private List<MasterSectionEntity> sections = new ArrayList<>();

    @Column(name = "`title`")
    private String title;

    @Column(name = "`subtitle`")
    private String subtitle;

    @Column(name = "`version`")
    private String version;

    @Column(name = "`created_at`")
    private ZonedDateTime createdAt;

    @Column(name = "`modified_at`")
    private ZonedDateTime modifiedAt;
    
    @NotNull
    @Column(name = "`default_contract`")
    private boolean defaultContract;

    public MasterContractDto toDto(boolean includeDetails) {
        final MasterContractDto c = new MasterContractDto();

        c.setCreatedAt(createdAt);
        c.setDefaultContract(defaultContract);
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
        e.setDefaultContract(h.isDefaultContract());
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
