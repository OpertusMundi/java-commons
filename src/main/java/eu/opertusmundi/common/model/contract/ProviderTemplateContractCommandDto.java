package eu.opertusmundi.common.model.contract;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProviderTemplateContractCommandDto {

    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private UUID providerKey;

}
