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

import eu.opertusmundi.common.model.contract.ContractDto;
import eu.opertusmundi.common.model.contract.EnumContractStatus;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractHistoryDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "ProviderContractHistory")
@Table(schema = "contract", name = "`provider_contract_history`")
public class ProviderTemplateContractHistoryEntity extends ProviderTemplateContractHistoryBaseEntity {

    @OneToOne(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    @Setter
    private ProviderTemplateContractDraftEntity draft;

    @OneToOne(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    @Setter
    private ProviderTemplateContractEntity published;

    @OneToMany(
        mappedBy = "contract", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true
    )
    @Getter
    @Setter
    private List<ProviderTemplateSectionHistoryEntity> sections = new ArrayList<>();

    public ContractDto toSimpleDto() {
        final ContractDto c = new ContractDto();

        c.setId(id);
        c.setKey(key);
        c.setTitle(title);
        c.setVersion(version);

        return c;
    }

    public ProviderTemplateContractHistoryDto toDto() {
        return this.toDto(false);
    }

    public ProviderTemplateContractHistoryDto toDto(boolean includeDetails) {
    	final ProviderTemplateContractHistoryDto c = new ProviderTemplateContractHistoryDto();

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
            c.setContractParentKey(contractParent == null ? null : contractParent.getKey());
            c.setContractRootKey(contractRoot == null ? null : contractRoot.getKey());
            c.setTemplateKey(template.getKey());

            c.setSections(sections.stream()
                .map(ProviderTemplateSectionHistoryEntity::toDto)
                .collect(Collectors.toList())
            );
        }

        return c;
    }

    public static ProviderTemplateContractHistoryEntity from(ProviderTemplateContractDraftEntity d) {
        final ProviderTemplateContractHistoryEntity e = new ProviderTemplateContractHistoryEntity();

        e.setContractParent(d.getParent());
        e.setContractRoot(d.getParent() == null ? null : d.getParent().getContractRoot());
        e.setCreatedAt(ZonedDateTime.now());
        e.setModifiedAt(e.getCreatedAt());
        e.setOwner(d.getOwner());
        e.setSections(d.getSections().stream().map(ProviderTemplateSectionHistoryEntity::from).collect(Collectors.toList()));
        e.setStatus(EnumContractStatus.INACTIVE);
        e.setSubtitle(d.getSubtitle());
        e.setTemplate(d.getTemplate());
        e.setTitle(d.getTitle());
        e.setVersion(d.getVersion());

        e.getSections().forEach(s -> s.setContract(e));

        return e;
    }

}
