package eu.opertusmundi.common.domain;

import java.util.List;

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

import eu.opertusmundi.common.model.contract.ContractSectionOptionDto;
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

    @NotNull
    @Column(name = "`variable`")
    @Getter
    @Setter
    private boolean variable;

    @NotNull
    @Column(name = "`optional`")
    @Getter
    @Setter
    private boolean optional;

    @NotNull
    @Column(name = "`dynamic`")
    @Getter
    @Setter
    private boolean dynamic;

    @Type(type = "json")
    @Column(
        name = "options",
        columnDefinition = "jsonb"
    )
    @Getter
    @Setter
    private List<ContractSectionOptionDto> options ;

    @Column(name = "`description_of_change`")
    @Getter
    @Setter
    private String descriptionOfChange;

    public MasterSectionDto toDto() {
        final MasterSectionDto s = new MasterSectionDto();

        s.setDescriptionOfChange(descriptionOfChange);
        s.setDynamic(dynamic);
        s.setId(id);
        s.setIndent(indent);
        s.setIndex(index);
        s.setOptional(optional);
        s.setOptions(options);
        s.setTitle(title);
        s.setVariable(variable);

        return s;
    }

    public static MasterSectionEntity from(MasterSectionHistoryEntity s) {
        final MasterSectionEntity e = new MasterSectionEntity();

        e.setDescriptionOfChange(s.getDescriptionOfChange());
        e.setDynamic(s.isDynamic());
        e.setIndent(s.getIndent());
        e.setIndex(s.getIndex());
        e.setOptional(s.isOptional());
        e.setOptions(s.getOptions());
        e.setTitle(s.getTitle());
        e.setVariable(s.isVariable());

        return e;
    }

}
