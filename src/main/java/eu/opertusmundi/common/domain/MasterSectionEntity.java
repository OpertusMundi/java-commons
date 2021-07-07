package eu.opertusmundi.common.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import eu.opertusmundi.common.model.contract.MasterSectionDto;


@TypeDef(name = "json", typeClass = JsonBinaryType.class)
@TypeDef(
	    name = "list-array",
	    typeClass = ListArrayType.class)
@Table(
    schema = "contract", name = "`master_section`"
)
@Entity(name = "Section")
public class MasterSectionEntity {
	
    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(
        sequenceName = "contract.master_section_id_seq", name = "master_section_id_seq", allocationSize = 1
    )
    @GeneratedValue(generator = "master_section_id_seq", strategy = GenerationType.SEQUENCE)
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
    MasterContractEntity contract;
    
    @Column(name = "`indent`")
    @lombok.Getter()
    @lombok.Setter()
    Integer indent;
    
    @NotNull
    @Size(max = 80)
    @Column(name = "`index`", updatable = true)
    @lombok.Getter()
    @lombok.Setter()
    String index;

    @Column(name = "`title`")
    @lombok.Getter()
    @lombok.Setter()
    String title;
    
    @Column(name = "`variable`")
    @lombok.Getter()
    @lombok.Setter()
    Boolean variable;
    
    @Column(name = "`optional`")
    @lombok.Getter()
    @lombok.Setter()
    Boolean optional;
    
    @Column(name = "`dynamic`")
    @lombok.Getter()
    @lombok.Setter()
    Boolean dynamic;
    
    @Type(type = "list-array")
    @Column(
        name = "options",
        columnDefinition = "text[]"
    )
    @lombok.Getter()
    @lombok.Setter()
    List<String> options ;
    
    @Type(type = "list-array")
    @Column(
        name = "styled_options",
        columnDefinition = "text[]"
    )
    @lombok.Getter()
    @lombok.Setter()
    List<String> styledOptions ;
    
    @Type(type = "json")
    @Column(
        name = "suboptions"
    )
    @lombok.Getter()
    @lombok.Setter()
    Map<Integer, Object> suboptions =  new HashMap<Integer, Object>();
    
    @Type(type = "list-array")
    @Column(
        name = "summary",
        columnDefinition = "text[]"
    )
    @lombok.Getter()
    @lombok.Setter()
    List<String> summary ;


    @Type(type = "list-array")
    @Column(
    	name="icons",
    	columnDefinition = "text")
    @lombok.Getter()
    @lombok.Setter()
    List <String> icons;
    
    @Column(name = "`description_of_change`")
    @lombok.Getter()
    @lombok.Setter()
    String descriptionOfChange;

    public MasterSectionDto toDto() {
    	MasterSectionDto s = new MasterSectionDto();

        s.setId(id);
        s.setTitle(title);
        s.setVariable(variable);
        s.setOptional(optional);
        s.setDynamic(dynamic);
        s.setIndex(index);
        s.setIndent(indent);
        s.setSummary(summary);
        s.setOptions(options);
        s.setStyledOptions(styledOptions);
        s.setSuboptions(suboptions);
        s.setIcons(icons);
        s.setDescriptionOfChange(descriptionOfChange);
        return s;
    }

}
