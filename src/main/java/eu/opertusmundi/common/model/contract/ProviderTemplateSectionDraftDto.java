package eu.opertusmundi.common.model.contract;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Data
public class ProviderTemplateSectionDraftDto {
	
	private Integer id;
	
	@NotNull
	private ProviderTemplateContractDraftDto contract;

	private Integer master_section_id;
	
	private boolean optional;
	
	private Integer option;
	
	private Integer suboption;
}
