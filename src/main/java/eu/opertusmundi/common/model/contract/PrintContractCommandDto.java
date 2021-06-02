package eu.opertusmundi.common.model.contract;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class PrintContractCommandDto {

    private EnumContract type;

    private UUID providerKey;

    private UUID userKey;

    private String assetId;
}
