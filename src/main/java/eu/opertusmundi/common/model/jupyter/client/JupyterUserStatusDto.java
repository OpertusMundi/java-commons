package eu.opertusmundi.common.model.jupyter.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Builder
public class JupyterUserStatusDto {

    @Schema(description = "True if the server is ready")
    private boolean ready;

    @Schema(description = "The URL to the running notebook server")
    private String path;

    @Schema(description = "Profile of running server")
    private String profile;

    public static JupyterUserStatusDto empty() {
        return new JupyterUserStatusDto();
    }
}
