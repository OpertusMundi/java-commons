package eu.opertusmundi.common.model.asset;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AssetUploadedContractCommandDto extends ResourceCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "File description")
    private String description;

    @Schema(description = "File name. If not set, the name of uploaded file is used.")
    private String fileName;

    /**
     * File size is set at the server
     */
    @JsonIgnore
    private Long size;
    
}
