package eu.opertusmundi.common.model.file;

import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CopyToDriveCommandDto {

    @Schema(description = "File path in user's remote file system")
    @NotEmpty
    private String path;

    private String fileName;

    @JsonIgnore
    private UUID userKey;

    @JsonIgnore
    private String pid;

    @JsonIgnore
    private String resourceKey;

}
