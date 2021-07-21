package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import eu.opertusmundi.common.model.contract.EnumContractStatus;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractHistoryDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "ContractHistory")
@Table(schema = "contract", name = "`master_contract_history`")
public class MasterContractHistoryEntity extends MasterContractHistoryBaseEntity {

    @OneToOne(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    @Setter
    private MasterContractDraftEntity draft;

    @OneToOne(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    @Setter
    private MasterContractEntity published;

    @OneToMany(
        mappedBy = "contract", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true
    )
    @Getter
    @Setter
    private List<MasterSectionHistoryEntity> sections = new ArrayList<>();

    public MasterSectionHistoryEntity findSectionById(int id) {
        return sections.stream()
            .filter(s -> s.getId() == id)
            .findFirst()
            .orElse(null);
    }
    
    public MasterContractHistoryDto toDto(boolean includeDetails) {
        final MasterContractHistoryDto c = new MasterContractHistoryDto();

        c.setCreatedAt(createdAt);
        c.setId(id);
        c.setKey(key);
        c.setModifiedAt(modifiedAt);
        c.setOwner(owner.toSimpleDto());
        c.setStatus(status);
        c.setSubtitle(subtitle);
        c.setTitle(title);
        c.setVersion(version);

        if (includeDetails) {
            c.setContractParentId(contractParent == null ? null : contractParent.getId());
            c.setContractRootId(contractRoot == null ? null : contractRoot.getId());

            c.setSections(sections.stream()
                .map(MasterSectionHistoryEntity::toDto)
                .collect(Collectors.toList())
            );
        }

        return c;
    }

    public static MasterContractHistoryEntity from(MasterContractDraftEntity d) {
        final MasterContractHistoryEntity e = new MasterContractHistoryEntity();

        e.setContractParent(d.getParent());
        e.setContractRoot(d.getParent() == null ? null : d.getParent().getContractRoot());
        e.setCreatedAt(ZonedDateTime.now());
        e.setModifiedAt(e.getCreatedAt());
        e.setOwner(d.getOwner());
        e.setSections(d.getSections().stream().map(MasterSectionHistoryEntity::from).collect(Collectors.toList()));
        e.setStatus(EnumContractStatus.INACTIVE);
        e.setSubtitle(d.getSubtitle());
        e.setTitle(d.getTitle());
        e.setVersion(d.getVersion());

        e.getSections().forEach(s -> s.setContract(e));

        return e;
    }

}
