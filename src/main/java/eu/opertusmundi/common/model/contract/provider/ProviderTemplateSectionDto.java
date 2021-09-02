package eu.opertusmundi.common.model.contract.provider;

import java.io.Serializable;

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
    private Integer option;

    @Schema(description = "Sub-option identifier")
    private Integer subOption;
}
