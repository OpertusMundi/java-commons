package eu.opertusmundi.common.model.jupyter.client;

import java.util.List;

import eu.opertusmundi.common.model.jupyter.JupyterHubProfile;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class JupyterConfigurationDto {

    @ArraySchema(
        arraySchema = @Schema(
            description = "Available profiles. A profile is accessible by one or more groups"
        ),
        minItems = 0
    )
    private List<JupyterHubProfile> profiles;

    @ArraySchema(
        arraySchema = @Schema(
            description = "User groups. A user can access a profile only if he belongs in one "
                        + "of the groups specified by the profile."
        ),
        minItems = 0
    )
    private List<String> groups;

    @Schema(description = "Profile of running server")
    private String activeProfile;

}
