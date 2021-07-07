package eu.opertusmundi.common.model.contract;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.account.helpdesk.HelpdeskAccountDto;
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
public class MasterContractHistoryDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Integer id;

    private UUID key;
    
	@JsonIgnore
	private Integer parentId;
	
	@NotEmpty
	private String title;
	
	
	private String subtitle;
	
	private String state;
	
	private String version;
	
	@JsonIgnore
	private HelpdeskAccountDto account;
	
	private List<MasterSectionHistoryDto> sections;
	
    private ZonedDateTime createdAt;
    
    private ZonedDateTime modifiedAt;
}
