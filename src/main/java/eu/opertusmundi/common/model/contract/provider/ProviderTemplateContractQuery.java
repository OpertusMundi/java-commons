package eu.opertusmundi.common.model.contract.provider;

import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.contract.EnumContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProviderTemplateContractQuery {

    private int page;

    private int size;

    @JsonIgnore
    private UUID providerKey;

    private Set<EnumContractStatus> status;

    private String title;

    private EnumProviderContractSortField orderBy;

    private EnumSortingOrder order;

}
