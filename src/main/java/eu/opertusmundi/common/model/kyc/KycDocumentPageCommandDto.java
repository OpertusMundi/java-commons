package eu.opertusmundi.common.model.kyc;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.account.EnumCustomerType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KycDocumentPageCommandDto {

    /**
     * Platform user unique key
     */
    @JsonIgnore
    private UUID userKey;

    /**
     * MANGOPAY KYC document unique identifier
     */
    @JsonIgnore
    private String kycDocumentId;

    /**
     * Uploaded file
     */
    @JsonIgnore
    private MultipartFile file;

    @JsonIgnore
    private Long fileSize;

    @JsonIgnore
    private String fileName;

    @JsonIgnore
    private String fileType;


    @JsonIgnore
    private String tag;

    @NotNull
    @Schema(
        description = "The customer type. For a consumer or provider, the authenticated user must "
                    + "have the `ROLE_CONSUMER` or `ROLE_PROVIDER` role respectively",
        required = true
    )
    private EnumCustomerType customerType;

    @Schema(description = "Optional comment")
    private String comment;

}
