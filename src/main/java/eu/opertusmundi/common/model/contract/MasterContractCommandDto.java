package eu.opertusmundi.common.model.contract;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasterContractCommandDto {

    @JsonIgnore
    private Integer id;
    
    private UUID key;
	
    @JsonIgnore
    private Integer parentId;
    
	@NotEmpty
	private String title;
	
	private String subtitle;
	
	private String state;
	
	private String version;
	
	private List<MasterSectionDto> sections;

}
