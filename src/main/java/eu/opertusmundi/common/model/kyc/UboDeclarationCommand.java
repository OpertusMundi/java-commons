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
public class UboDeclarationCommand {

    private UUID customerKey;

    private EnumCustomerType customerType;

    private String uboDeclarationId;

    public static UboDeclarationCommand of(UUID key, EnumCustomerType type) {
        return UboDeclarationCommand.builder()
            .customerKey(key)
            .customerType(type)
            .build();
    }

}
