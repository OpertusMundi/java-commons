package eu.opertusmundi.common.model.contract;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.Query;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProviderTemplateContractQueryDto extends Query {

    private Boolean active;

    @JsonIgnore
    private UUID providerKey;

}
