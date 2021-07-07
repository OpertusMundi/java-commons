package eu.opertusmundi.common.model.contract;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Data
public class MasterSectionDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Integer id;
	@NotNull
	private MasterContractDto contract;
	private Integer indent;
	private String index;
	@NotEmpty
	private String title;
	private boolean variable;
	private boolean optional;
	private boolean dynamic;
	private List<String> options;
	private List<String> styledOptions;
	private Map<Integer, Object> suboptions;
	private List<String> summary;
	private List<String> icons;
	private String descriptionOfChange;

}
