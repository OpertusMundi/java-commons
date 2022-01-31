package eu.opertusmundi.common.model.sinergise.server;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateContractResponse {

    private ContractDto contract;

    private UUID ogcInstanceId;

    private List<AuthClientDto> oAuthClients;
}
