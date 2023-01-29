package eu.opertusmundi.common.model.discovery.client;

import java.util.ArrayList;
import java.util.List;

import eu.opertusmundi.common.model.discovery.server.ServerJoinableTableResultDto;
import eu.opertusmundi.common.util.StreamUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientJoinableResultDto {

    private List<ClientJoinableTableDto> joinableTables = new ArrayList<>();

    public static ClientJoinableResultDto from(ServerJoinableTableResultDto s) {
        final var c = new ClientJoinableResultDto();

        StreamUtils.from(s.getJoinableTables())
            .map(ClientJoinableTableDto::from)
            .forEach(c.getJoinableTables()::add);

        return c;
    }

}
