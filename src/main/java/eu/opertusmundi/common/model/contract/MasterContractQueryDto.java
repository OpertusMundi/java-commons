package eu.opertusmundi.common.model.contract;

import eu.opertusmundi.common.model.Query;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasterContractQueryDto extends Query {

    private Boolean active;

}
