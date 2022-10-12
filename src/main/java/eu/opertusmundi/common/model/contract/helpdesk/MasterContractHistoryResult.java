package eu.opertusmundi.common.model.contract.helpdesk;

import java.util.List;

import eu.opertusmundi.common.model.PageRequestDto;
import eu.opertusmundi.common.model.PageResultDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasterContractHistoryResult extends PageResultDto<MasterContractHistoryDto> {

    public MasterContractHistoryResult() {
        super();
    }

    public MasterContractHistoryResult(PageRequestDto pageRequest) {
        super(pageRequest);
    }

    public MasterContractHistoryResult(
        PageRequestDto pageRequest, long count, List<MasterContractHistoryDto> items, int defaultContractCount
    ) {
        super(pageRequest, count, items);

        this.defaultContractCount = defaultContractCount;
    }

    private int defaultContractCount = 0;

}
