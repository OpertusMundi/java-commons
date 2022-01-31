package eu.opertusmundi.common.model.sinergise.server;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractResponseDto {

    private List<ContractDto> data;
}
