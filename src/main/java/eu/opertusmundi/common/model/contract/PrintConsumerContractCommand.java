package eu.opertusmundi.common.model.contract;

import java.nio.file.Path;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonIgnoreType
public class PrintConsumerContractCommand {

    private EnumContract type;

    private Integer userId;

    private UUID orderKey;

    private Integer itemIndex;

    private Path path;

}
