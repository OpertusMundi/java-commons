package eu.opertusmundi.common.model.asset;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFileResourceCommandDto extends ResourceCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Long size;

    @JsonIgnore
    private String userName;

    @Schema(description = "Geometry data CRS", example = "EPSG:4326")
    private String crs;

    @Schema(description = "File encoding", example = "UTF-8")
    private String encoding;

    @Schema(description =
        "Optional file name. If it is not set, the name of the selected file is used. "
      + "The user may rename a file to avoid name collisions e.g. if two files with the "
      + "same name but different paths are selected."
    )
    private String fileName;

    @Schema(description = "File format")
    @NotBlank
    private String format;

    @Schema(description = "Path to user's file system")
    @NotEmpty
    private String path;
}
