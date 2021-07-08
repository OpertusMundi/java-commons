package eu.opertusmundi.common.model.contract;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProviderTemplateContractCommandDto {

    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private UUID providerKey;
    
    private UUID key;
	
    @JsonIgnore
    private Integer parentId;
    
    private Integer masterContractId;
    
    private String masterContractVersion;
    
	private String title;
	
	private String subtitle;
	
	private String version;

	private Boolean active;
	
	private List<ProviderTemplateSectionDto> sections;
}
