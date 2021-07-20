package eu.opertusmundi.common.model.contract.consumer;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsumerContractCommandDto {

    private UUID userKey;

    private UUID providerKey;

    private String assetId;

}
