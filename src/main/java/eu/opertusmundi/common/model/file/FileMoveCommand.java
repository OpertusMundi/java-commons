package eu.opertusmundi.common.model.file;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(
    description = "File move/rename options. If both `targetFolder` and `targetFileName` are not set or the target "
                + "path is equal to the source path, the request is ignored"
)
@Getter
@Setter
public class FileMoveCommand {

    @Schema(
        description = "Absolute path in the user's remote file system for the source file",
        required = true,
        example = "/examples/data.csv"
    )
    @NotBlank
    private String sourcePath;

    @Schema(
        description = "Absolute path in the user's remote file system for the target folder. If the property is "
                    + "not set, the folder path from `sourcePath` property is used",
        required = false,
        example = "/demo"
    )
    private String targetFolder;

    @Schema(
        description = "The new file name. If the property is not set, the file name from the `sourcePath` property is used.",
        required = false,
        example = "samples.csv"
    )
    private String targetFileName;

    @Schema(description = "True if an existing file should be overwritten", required = false, defaultValue = "false")
    private boolean overwrite = false;

    @JsonIgnore
    private String userName;
}
