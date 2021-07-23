package eu.opertusmundi.common.model.contract;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractSectionOptionDto{
	
	@Getter
	@Setter
    private String body;
	
	@Getter
	@Setter
    private String bodyHtml;
	
	@Getter
	@Setter
    private List<ContractSectionSubOptionDto> subOptions;
	
	@Getter
	@Setter
	private String summary;
	
	@Getter
	@Setter
	private String icon;
}
