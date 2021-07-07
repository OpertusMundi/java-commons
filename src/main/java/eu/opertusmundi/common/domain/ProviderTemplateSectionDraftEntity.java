package eu.opertusmundi.common.domain;



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

import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import eu.opertusmundi.common.model.contract.ProviderTemplateSectionDraftDto;


@TypeDef(name = "json", typeClass = JsonBinaryType.class)
@TypeDef(
	    name = "list-array",
	    typeClass = ListArrayType.class)
@Table(
    schema = "contract", name = "`provider_section_draft`"
)
@Entity(name = "ProviderSectionDraft")
public class ProviderTemplateSectionDraftEntity{
	
    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(
        sequenceName = "contract.provider_section_id_seq", name = "provider_section_id_seq", allocationSize = 1
    )
    @GeneratedValue(generator = "provider_section_id_seq", strategy = GenerationType.SEQUENCE)
    @lombok.Setter()
    @lombok.Getter()
    Integer id ;

    @NotNull
    @ManyToOne(
		fetch = FetchType.EAGER
	)
    @JoinColumn(name = "`contract`", nullable = false)
    @lombok.Getter
    @lombok.Setter
    ProviderTemplateContractDraftEntity contract;
    
    @Column(name = "`master_section_id`", updatable = false)
    @lombok.Setter()
    @lombok.Getter()
    Integer masterSectionId ;
    
    
    @Column(name = "`optional`")
    @lombok.Getter()
    @lombok.Setter()
    Boolean optional;
    
    @Column(name = "`option`", updatable = false)
    @lombok.Setter()
    @lombok.Getter()
    Integer option ;
    
    @Column(name = "`suboption`", updatable = false)
    @lombok.Setter()
    @lombok.Getter()
    Integer suboption ;
    

    public ProviderTemplateSectionDraftDto toDto() {
    	ProviderTemplateSectionDraftDto s = new ProviderTemplateSectionDraftDto();

        s.setId(id);
        s.setMaster_section_id(masterSectionId);
        s.setOptional(optional);
        s.setOption(option);
        s.setSuboption(suboption);
        return s;
    }

}
