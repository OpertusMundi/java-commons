package eu.opertusmundi.common.model.contract.consumer;

import java.nio.file.Path;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import eu.opertusmundi.common.model.contract.EnumContract;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonIgnoreType
public class ConsumerContractCommand {

    private EnumContract type;

    private Integer userId;

    private UUID orderKey;

    private Integer itemIndex;

    private Path path;
    
    private boolean draft;

}
