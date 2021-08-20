package eu.opertusmundi.common.model.catalogue.client;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UnpublishAssetCommand {

    private UUID userKey;

    private UUID publisherKey;

    private String pid;
    
}
