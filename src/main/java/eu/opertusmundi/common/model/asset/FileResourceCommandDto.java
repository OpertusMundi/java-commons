package eu.opertusmundi.common.model.asset;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class FileResourceCommandDto extends ResourceCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private EnumAssetType category;

    @JsonIgnore
    private Long size;

    @Schema(description = "Geometry data CRS", example = "EPSG:4326")
    private String crs;

    @Schema(description = "File encoding", example = "UTF-8")
    private String encoding;

    @Schema(description = "File name. If not set, the name of uploaded file is used.")
    private String fileName;

    @Schema(description = "File format")
    @NotBlank
    private String format;

}
