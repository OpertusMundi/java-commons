package eu.opertusmundi.common.model.file;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class FilePathCommand {

    @Schema(
        description = "An absolute path in user's remote file system. A leading slash is optional",
        example = "/tutorial",
        required = true
    )
    @NotEmpty
    private String path;

    @JsonIgnore
    private String userName;

}
