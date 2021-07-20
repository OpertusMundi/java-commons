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

import eu.opertusmundi.common.model.contract.helpdesk.MasterSectionDto;
import lombok.Getter;
import lombok.Setter;


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
    @Getter
    private Integer id ;

    @NotNull
    @ManyToOne(
		fetch = FetchType.EAGER
	)
    @JoinColumn(name = "`contract`", nullable = false)
    @Getter
    @Setter
    private MasterContractEntity contract;

    @Column(name = "`indent`")
    @Getter
    @Setter
    private Integer indent;

    @NotNull
    @Size(max = 80)
    @Column(name = "`index`", updatable = true)
    @Getter
    @Setter
    private String index;

    @Column(name = "`title`")
    @Getter
    @Setter
    private String title;

    @Column(name = "`variable`")
    @Getter
    @Setter
    private Boolean variable;

    @Column(name = "`optional`")
    @Getter
    @Setter
    private Boolean optional;

    @Column(name = "`dynamic`")
    @Getter
    @Setter
    private Boolean dynamic;

    @Type(type = "list-array")
    @Column(
        name = "options",
        columnDefinition = "text[]"
    )
    @Getter
    @Setter
    private List<String> options ;

    @Type(type = "list-array")
    @Column(
        name = "styled_options",
        columnDefinition = "text[]"
    )
    @Getter
    @Setter
    private List<String> styledOptions ;

    @Type(type = "json")
    @Column(
        name = "sub_options"
    )
    @Getter
    @Setter
    private Map<Integer, Object> subOptions =  new HashMap<Integer, Object>();

    @Type(type = "list-array")
    @Column(
        name = "summary",
        columnDefinition = "text[]"
    )
    @Getter
    @Setter
    private List<String> summary ;


    @Type(type = "list-array")
    @Column(
    	name="icons",
    	columnDefinition = "text")
    @Getter
    @Setter
    private List <String> icons;

    @Column(name = "`description_of_change`")
    @Getter
    @Setter
    private String descriptionOfChange;

    public MasterSectionDto toDto() {
        MasterSectionDto s = new MasterSectionDto();

        s.setDescriptionOfChange(descriptionOfChange);
        s.setDynamic(dynamic);
        s.setIcons(icons);
        s.setId(id);
        s.setIndent(indent);
        s.setIndex(index);
        s.setOptional(optional);
        s.setOptions(options);
        s.setStyledOptions(styledOptions);
        s.setSubOptions(subOptions);
        s.setSummary(summary);
        s.setTitle(title);
        s.setVariable(variable);

        return s;
    }

    public static MasterSectionEntity from(MasterSectionHistoryEntity s) {
        final MasterSectionEntity e = new MasterSectionEntity();

        e.setDescriptionOfChange(s.getDescriptionOfChange());
        e.setDynamic(s.getDynamic());
        e.setIcons(s.getIcons());
        e.setIndent(s.getIndent());
        e.setIndex(s.getIndex());
        e.setOptional(s.getOptional());
        e.setOptions(s.getOptions());
        e.setStyledOptions(s.getStyledOptions());
        e.setSubOptions(s.getSubOptions());
        e.setSummary(s.getSummary());
        e.setTitle(s.getTitle());
        e.setVariable(s.getVariable());

        return e;
    }

}
