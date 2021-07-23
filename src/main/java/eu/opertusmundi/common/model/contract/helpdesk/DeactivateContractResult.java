package eu.opertusmundi.common.model.contract.helpdesk;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public class DeactivateContractResult {

    private Integer contractId;

    private MasterContractHistoryDto contract;

}
