package eu.opertusmundi.common.model.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(staticName = "of")
@Builder
@Getter
public class CopyToDriveResultDto {

    @Schema(description = "`true` if the copy operation is asynchronous")
    private final boolean async;

}
