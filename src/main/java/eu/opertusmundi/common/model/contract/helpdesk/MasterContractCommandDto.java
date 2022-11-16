package eu.opertusmundi.common.model.contract.helpdesk;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public class MasterContractCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private Integer userId;

    @Schema(description = "Master contract provider key")
    private UUID providerKey;

    @Schema(description = "Title")
    @NotEmpty
    private String title;

    @Schema(description = "Subtitle")
    private String subtitle;

    @Schema(description = "Sections")
    @ArraySchema(
        arraySchema = @Schema(
            description = "Contract sections"
        ),
        minItems = 0
    )
    private List<MasterSectionDto> sections;

}
