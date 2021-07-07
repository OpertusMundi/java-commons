package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.contract.MasterContractDraftDto;
import eu.opertusmundi.common.model.contract.MasterContractHistoryDto;
import lombok.Getter;


@Entity(name = "ContractDraft")
@Table(
    schema = "contract", name = "`master_contract_draft`"
)
public class MasterContractDraftEntity {
	
    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(
        sequenceName = "contract.master_contract_id_seq", name = "master_contract_id_seq", allocationSize = 1
    )
    @GeneratedValue(generator = "master_contract_id_seq", strategy = GenerationType.SEQUENCE)
    @lombok.Setter()
    @lombok.Getter()
    Integer id ;
    
    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Getter
    private final UUID key = UUID.randomUUID();
    
    @Column(name = "`parent_id`")
    @lombok.Getter
    @lombok.Setter
    Integer parentId;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "`account`", nullable = false)
    @lombok.Getter
    @lombok.Setter
    HelpdeskAccountEntity account;

    @Column(name = "`title`")
    @lombok.Getter()
    @lombok.Setter()
    String title;

    @Column(name = "`subtitle`")
    @lombok.Getter
    @lombok.Setter
    String subtitle;

    @Column(name = "`state`")
    @lombok.Getter
    @lombok.Setter
    String state;

    @Column(name = "`version`")
    @lombok.Getter
    @lombok.Setter
    String version;


    @Column(name = "`created_at`")
    @lombok.Getter
    @lombok.Setter
    ZonedDateTime createdAt;


    @Column(name = "`modified_at`")
    @lombok.Getter
    @lombok.Setter
    ZonedDateTime modifiedAt;
    
   /*@OneToMany(
        mappedBy = "contract", 
        fetch = FetchType.LAZY,
        targetEntity = SectionEntity.class
    )
    @lombok.Getter()
    @lombok.Setter()
    List<SectionEntity> sections = new ArrayList<>();*/

    public MasterContractDraftDto toDto() {
    	MasterContractDraftDto c = new MasterContractDraftDto();

        c.setId(this.id);
        c.setParentId(this.getParentId());
        c.setTitle(this.title);
        c.setSubtitle(this.subtitle);
        c.setState(this.state);
        c.setAccount(this.account.toDto());
        c.setCreatedAt(this.createdAt);
        c.setModifiedAt(this.modifiedAt);
        c.setVersion(this.version);
        //c.setSections(this.sections);
        
        return c;
    }

}
