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

import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "ProviderContract")
@Table(
    schema = "contract", name = "`provider_contract`"
)
@Getter
@Setter
public class ProviderTemplateContractEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(
        sequenceName = "contract.provider_contract_id_seq", name = "provider_contract_id_seq", allocationSize = 1
    )
    @GeneratedValue(generator = "provider_contract_id_seq", strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PRIVATE)
    private Integer id ;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    private UUID key;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`owner`", nullable = false)
    private AccountEntity owner;

    @NotNull
    @OneToOne(
        optional = true, fetch = FetchType.LAZY, orphanRemoval = false
    )
    @JoinColumn(name = "`parent`")
    private ProviderTemplateContractHistoryEntity parent;

    @OneToMany(
        mappedBy = "contract", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true
    )
    private List<ProviderTemplateSectionEntity> sections = new ArrayList<>();

    @Column(name = "`title`")
    private String title;

    @Column(name = "`subtitle`")
    private String subtitle;

    @Column(name = "`version`")
    private String version;

    @Column(name = "`created_at`")
    private ZonedDateTime createdAt;

    @Column(name = "`modified_at`")
    ZonedDateTime modifiedAt;

    @NotNull
    @Column(name = "`default_contract`")
    private boolean defaultContract;
        
    @NotNull
    @Column(name = "`default_contract_accepted`")
    private boolean defaultContractAccepted;
    
    public ProviderTemplateContractDto toDto(boolean includeDetails) {
        final ProviderTemplateContractDto c = new ProviderTemplateContractDto();

        c.setCreatedAt(createdAt);
        c.setDefaultContract(defaultContract);
        c.setDefaultContractAccepted(defaultContractAccepted);
        c.setId(id);
        c.setKey(key);
        c.setModifiedAt(modifiedAt);
        c.setSubtitle(subtitle);
        c.setTemplateKey(parent.getTemplate().getKey());
        c.setTitle(title);
        c.setVersion(version);

        if (includeDetails) {
            c.setContractParentKey(parent.getKey());
            c.setContractRootKey(parent.getContractRoot().getKey());
            c.setMasterContract(parent.getTemplate().toDto(true));

            c.setSections(sections.stream()
                .map(ProviderTemplateSectionEntity::toDto)
                .collect(Collectors.toList())
            );

            c.getMasterContract().removeHelpdeskData();
        }

        return c;
    }

    public static ProviderTemplateContractEntity from(ProviderTemplateContractHistoryEntity h) {
        final ProviderTemplateContractEntity e = new ProviderTemplateContractEntity();

        e.setCreatedAt(h.getCreatedAt());
        e.setDefaultContract(h.isDefaultContract());
        e.setDefaultContractAccepted(h.isDefaultContractAccepted());
        e.setKey(h.getKey());
        e.setModifiedAt(h.getModifiedAt());
        e.setOwner(h.getOwner());
        e.setParent(h);
        e.setSections(h.getSections().stream().map(ProviderTemplateSectionEntity::from).collect(Collectors.toList()));
        e.setSubtitle(h.getSubtitle());
        e.setTitle(h.getTitle());
        e.setVersion(h.getVersion());

        e.getSections().forEach(s -> s.setContract(e));

        return e;
    }

}
