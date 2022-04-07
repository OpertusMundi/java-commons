package eu.opertusmundi.common.model.contract.provider;

import java.util.UUID;

import javax.validation.constraints.NotEmpty;

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
public class ProviderUploadedContractCommand {

    /**
     * Asset unique key. This value is injected by the controller.
     */
    @JsonIgnore
    private UUID draftKey;
    
    /**
     * Publisher unique key
     */
	@JsonIgnore
    private UUID publisherKey;

    /**
     * The authenticated user key
     */
    @JsonIgnore
    private UUID ownerKey;

    /**
     * True if the record must be locked when the command executes
     */
    @JsonIgnore
    private boolean locked;
    
    @Schema(description = "File name. If not set, the name of uploaded file is used.")
    private String fileName;

    /**
     * File size is set at the server
     */
    @JsonIgnore
    private Long size;
    
}
