package eu.opertusmundi.common.model.contract.provider;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Data
public class ProviderTemplateSectionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    @Schema(description = "Master contract section identifier")
    private Integer masterSectionId;

    @Schema(description = "Optional")
    private boolean optional;

    @Schema(description = "Option identifier")
    @NotNull
    private Integer option;

    @Schema(description = "Sub-option identifiers")
    private List<Integer> subOption;

}
