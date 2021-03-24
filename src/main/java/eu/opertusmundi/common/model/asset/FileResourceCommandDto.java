package eu.opertusmundi.common.model.asset;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class FileResourceCommandDto extends BaseResourceCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Schema(description = "File format")
    @NotBlank
    private String format;

    @Schema(description = "File name. If not set, the name of uploaded file is used.")
    private String fileName;

    /**
     * Computed from the format value
     */
    @JsonIgnore
    private EnumAssetSourceType category;
    
    /**
     * File size is set at the server
     */
    @JsonIgnore
    private Long size;
    
}
