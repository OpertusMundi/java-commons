package eu.opertusmundi.common.model.kyc;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import eu.opertusmundi.common.model.account.EnumCustomerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor(staticName = "of")
@Getter
@Setter
@Builder
@ToString
@JsonIgnoreType
public class KycDocumentCommand {

    private UUID customerKey;
    
    private EnumCustomerType customerType;

    private String kycDocumentId;

    public static KycDocumentCommand of(UUID customerKey) {
        return KycDocumentCommand.builder().customerKey(customerKey).build();
    }

}
