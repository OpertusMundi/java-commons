package eu.opertusmundi.common.model.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(staticName = "of")
@Builder
@Getter
public class CopyToDriveResultDto {

    private final boolean async;

}
