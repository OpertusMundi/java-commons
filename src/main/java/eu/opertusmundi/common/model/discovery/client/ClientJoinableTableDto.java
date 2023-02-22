package eu.opertusmundi.common.model.discovery.client;

import java.util.ArrayList;
import java.util.List;

import eu.opertusmundi.common.model.discovery.server.ServerJoinableTableDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientJoinableTableDto {

    private List<ClientJoinableTableMatchDto> matches = new ArrayList<>();

    private String tableName;
    private String tablePath;

    public static ClientJoinableTableDto from(ServerJoinableTableDto s) {
        final var c = new ClientJoinableTableDto();

        c.setTableName(s.getTableName());
        c.setTablePath(s.getTablePath());
        s.getMatches().stream()
            .map(ClientJoinableTableMatchDto::from)
            .forEach(c.getMatches()::add);

        return c;
    }

}
