package eu.opertusmundi.common.model.kyc;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

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

    private UUID providerKey;

    private String uboDeclarationId;

    public static UboDeclarationCommand of(UUID providerKey) {
        return UboDeclarationCommand.builder().providerKey(providerKey).build();
    }

}
