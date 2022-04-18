package eu.opertusmundi.common.model.order;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonIgnoreType
public class UploadOrderContractCommand {

    @JsonIgnore
    private UUID orderKey;
    
    @Schema(description = "File name. If not set, the name of uploaded file is used.")
    private String fileName;

    /**
     * File size is set at the server
     */
    @JsonIgnore
    private Long size;
    

}
