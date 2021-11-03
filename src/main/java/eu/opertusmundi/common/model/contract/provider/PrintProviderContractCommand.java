package eu.opertusmundi.common.model.contract.provider;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonIgnoreType
public class PrintProviderContractCommand {

    private UUID providerKey;
    
    private UUID contractKey;
    


}
