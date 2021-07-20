package eu.opertusmundi.common.model.contract.helpdesk;

import java.util.Set;

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
public class MasterContractQueryDto {

    private int page;

    private int size;

    private Set<EnumContractStatus> status;
    
    private String title;

    private EnumMasterContractSortField orderBy;

    private EnumSortingOrder order;

}
