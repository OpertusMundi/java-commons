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
import eu.opertusmundi.common.model.contract.TemplateContractDto;
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

    public List<ProviderTemplateSectionHistoryEntity> getSectionsSorted() {
    	return this.sections.stream().sorted((s1,s2) -> {
    		/* Get index of each section*/
    		final String s1Index	=	s1.getMasterSection().getIndex();
    		final String s2Index	=	s2.getMasterSection().getIndex();
    	    /* NumValue stores each numeric part of version*/
    	    int numValue1 = 0;
    	    int numValue2 = 0;
    	    /* Loop until both string are processed*/
    	    for (int i = 0, j = 0 ; (i < s1Index.length() || j < s2Index.length()) ;) {
    	        /* Store numeric part of section index 1 in numValue1*/
    	        while (i < s1Index.length() && s1Index.charAt(i) != '.') {
    	        	numValue1 = numValue1 * 10 + (s1Index.charAt(i) - '0');
    	            i++;
    	        }
    	        /* Store numeric part of section index 2 in numValue2*/
    	        while (j < s2Index.length() && s2Index.charAt(j) != '.') {
    	        	numValue2 = numValue2 * 10 + (s2Index.charAt(j) - '0');
    	            j++;
    	        }
    	        /* Compare values*/
    	        if (numValue1 > numValue2) {
    	            return 1;
    	        }
    	        else if (numValue1 < numValue2) {
    	            return -1;
    	        }
    	        /* if values are equal, reset variables and go for next numeric part*/
    	        else {
    	        	numValue1 = numValue2 = 0;
        	        i++;
        	        j++;
    	        }
    	    }
    	    /* Return 0 if sections are equal. It will never happen*/
    	    return 0;
    	}).collect(Collectors.toList());
    }

    public TemplateContractDto toSimpleDto() {
        final TemplateContractDto c = new TemplateContractDto();

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
    	c.setDefaultContract(defaultContract);
    	c.setDefaultContractAccepted(defaultContractAccepted);
    	c.setDefaultContractAcceptedAt(defaultContractAcceptedAt);
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
        e.setDefaultContract(d.isDefaultContract());
        e.setDefaultContractAccepted(false);
        e.setDefaultContractAcceptedAt(null);
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
