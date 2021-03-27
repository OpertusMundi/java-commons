package eu.opertusmundi.common.model.kyc;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.dto.EnumCustomerType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KycDocumentPageCommandDto {

    @JsonIgnore
    private UUID userKey;

    @NotNull
    @Schema(
        description = "The customer type. For a consumer or provider, the authenticated user must "
                    + "have the `ROLE_CONSUMER` or `ROLE_PROVIDER` role respectively", 
        required = true
    )
    private EnumCustomerType customerType;

    @JsonIgnore
    private String kycDocumentId;

    @JsonIgnore
    private Long fileSize;

    @JsonIgnore
    private String fileName;

    @JsonIgnore
    private String fileType;

    @Schema(description = "Optional comment")
    private String comment;
    
    @JsonIgnore
    private String tag;

}
