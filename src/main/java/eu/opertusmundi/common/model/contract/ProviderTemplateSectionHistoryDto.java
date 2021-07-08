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
public class ProviderTemplateSectionHistoryDto {
	
	private Integer id;
	
	@NotNull
	private ProviderTemplateContractHistoryDto contract;

	private Integer masterSectionId;
	
	private boolean optional;
	
	private Integer option;
	
	private Integer suboption;
}
