package eu.opertusmundi.common.model.contract.provider;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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
public class ProviderTemplateContractCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private UUID draftKey;

    @JsonIgnore
    private Integer userId;

    @JsonIgnore
    private UUID userKey;

    @Schema(description = "Master template key")
    @NotNull
    private UUID templateKey;

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
    @Valid
    private List<ProviderTemplateSectionDto> sections;
}
