package eu.opertusmundi.common.model.contract;

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
public class ProviderTemplateContractQuery {

    private Boolean active;

    private UUID providerKey;

}
