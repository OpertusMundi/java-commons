package eu.opertusmundi.common.model.contract.helpdesk;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Data
public class MasterSectionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer indent;

    private String index;

    @NotEmpty
    private String title;

    private boolean variable;

    private boolean optional;

    private boolean dynamic;

    private List<String> options;

    private Map<Integer, Object> subOptions;

    private List<String> styledOptions;

    private List<String> summary;

    private List<String> icons;

    private String descriptionOfChange;

}
