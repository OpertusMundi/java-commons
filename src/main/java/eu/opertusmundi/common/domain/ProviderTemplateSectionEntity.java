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

import eu.opertusmundi.common.model.contract.provider.ProviderTemplateSectionDto;
import lombok.Getter;
import lombok.Setter;


@TypeDef(name = "json", typeClass = JsonBinaryType.class)
@TypeDef(
	    name = "list-array",
	    typeClass = ListArrayType.class)
@Table(
    schema = "contract", name = "`provider_section`"
)
@Entity(name = "ProviderSection")
public class ProviderTemplateSectionEntity{


    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(
        sequenceName = "contract.provider_section_id_seq", name = "provider_section_id_seq", allocationSize = 1
    )
    @GeneratedValue(generator = "provider_section_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id ;

    @NotNull
    @ManyToOne(
		fetch = FetchType.EAGER
	)
    @JoinColumn(name = "`contract`", nullable = false)
    @Getter
    @Setter
    private ProviderTemplateContractEntity contract;

    @Column(name = "`master_section_id`", updatable = false)
    @Setter
    @Getter
    private Integer masterSectionId ;

    @Column(name = "`optional`")
    @Getter
    @Setter
    private Boolean optional;

    @Column(name = "`option`", updatable = false)
    @Setter
    @Getter
    private Integer option ;

    @Column(name = "`sub_option`", updatable = false)
    @Setter
    @Getter
    private Integer subOption ;

    public ProviderTemplateSectionDto toDto() {
        final ProviderTemplateSectionDto s = new ProviderTemplateSectionDto();

        s.setId(id);
        s.setMasterSectionId(masterSectionId);
        s.setOption(option);
        s.setOptional(optional);
        s.setSubOption(subOption);

        return s;
    }

    public static ProviderTemplateSectionEntity from(ProviderTemplateSectionHistoryEntity s) {
        final ProviderTemplateSectionEntity e = new ProviderTemplateSectionEntity();

        e.setMasterSectionId(s.getMasterSectionId());
        e.setOption(s.getOption());
        e.setOptional(s.getOptional());
        e.setSubOption(s.getSubOption());

        return e;
    }

}
