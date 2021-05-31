package eu.opertusmundi.common.model.kyc;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.account.EnumCustomerType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KycDocumentCommandDto {

    @JsonIgnore
    private UUID userKey;

    @NotNull
    @Schema(
        description = "The customer type. For a consumer or provider, the authenticated user must "
                    + "have the `ROLE_CONSUMER` or `ROLE_PROVIDER` role respectively", 
        required = true
    )
    private EnumCustomerType customerType;

    @NotNull
    @Schema(description = "The type of the KYC document", required = true)
    private EnumKycDocumentType type;

    @JsonIgnore
    private String tag;

}
