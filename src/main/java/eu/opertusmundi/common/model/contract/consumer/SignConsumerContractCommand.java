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
public class SignConsumerContractCommand {

    private EnumContract type;

    private Integer userId;

    private UUID orderKey;

    private Integer itemIndex;

    /**
     * Initial contract path. Path must exist and be a file
     */
    private Path sourcePath;

    /**
     * Signed contract path. Path must not exist.
     */
    private Path targetPath;

}
