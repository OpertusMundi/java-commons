package eu.opertusmundi.common.model.contract;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Data
@Getter
@Setter
public class ProviderTemplateContractDraftDto {

	
	private Integer id;
	
    private UUID key;
	
    private UUID providerKey;
    
    @JsonIgnore
    private Integer parentId;
    
    private Integer masterContractId;
    
    private String masterContractVersion;
    
	@NotEmpty
	private String title;
	
	private String subtitle;
	
	private String version;

	private Boolean active;
	
	private List<ProviderTemplateSectionDraftDto> sections;
	
    private ZonedDateTime createdAt;
    
    private ZonedDateTime modifiedAt;
	
}
