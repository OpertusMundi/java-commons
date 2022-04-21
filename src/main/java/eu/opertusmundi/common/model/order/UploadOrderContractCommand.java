package eu.opertusmundi.common.model.order;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(staticName = "of")
@Getter
@Builder
@JsonIgnoreType
public class UploadOrderContractCommand {

    private final UUID providerKey;

    private final UUID orderKey;

    private final Integer itemIndex;

    private final String fileName;

    private final Long size;

    private final boolean lastUpdate;

}
