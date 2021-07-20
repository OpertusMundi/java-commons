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

import eu.opertusmundi.common.model.contract.helpdesk.MasterSectionDto;
import lombok.Getter;
import lombok.Setter;


@TypeDef(
    name = "list-array",
    typeClass = ListArrayType.class)
@Table(
    schema = "contract", name = "`master_section_history`")
@Entity(name = "SectionHistory")
public class MasterSectionHistoryEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(
        sequenceName = "contract.master_section_hist_id_seq", name = "master_section_hist_id_seq", allocationSize = 1
    )
    @GeneratedValue(generator = "master_section_hist_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id ;

    @NotNull
    @ManyToOne(
		fetch = FetchType.EAGER
	)
    @JoinColumn(name = "`contract`", nullable = false)
    @Getter
    @Setter
    private MasterContractHistoryEntity contract;

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

    public static MasterSectionHistoryEntity from(MasterSectionDraftEntity d) {
        final MasterSectionHistoryEntity e = new MasterSectionHistoryEntity();

        e.setDescriptionOfChange(d.getDescriptionOfChange());
        e.setDynamic(d.getDynamic());
        e.setIcons(d.getIcons());
        e.setIndent(d.getIndent());
        e.setIndex(d.getIndex());
        e.setOptional(d.getOptional());
        e.setOptions(d.getOptions());
        e.setStyledOptions(d.getStyledOptions());
        e.setSubOptions(d.getSubOptions());
        e.setSummary(d.getSummary());
        e.setTitle(d.getTitle());
        e.setVariable(d.getVariable());

        return e;
    }

}
