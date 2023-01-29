package eu.opertusmundi.common.model.discovery.client;

import java.util.ArrayList;
import java.util.List;

import eu.opertusmundi.common.model.discovery.server.ServerRelatedTableResultDto;
import eu.opertusmundi.common.util.StreamUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientRelatedResultDto {

    private List<ClientRelatedTableDto> relatedTables = new ArrayList<>();

    public static ClientRelatedResultDto from(ServerRelatedTableResultDto s) {
        final var c = new ClientRelatedResultDto();

        StreamUtils.from(s.getRelatedTables()).map(ClientRelatedTableDto::from).forEach(c.getRelatedTables()::add);

        return c;
    }

}
