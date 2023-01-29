package eu.opertusmundi.common.model.discovery.client;

import java.util.ArrayList;
import java.util.List;

import eu.opertusmundi.common.model.discovery.server.ServerRelatedTableDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientRelatedTableDto {

    private List<String> links = new ArrayList<>();
    private String       explanation;

    public static ClientRelatedTableDto from(ServerRelatedTableDto s) {
        final var c = new ClientRelatedTableDto();

        c.setExplanation(s.getExplanation());
        if (s.getLinks() != null) {
            c.getLinks().addAll(s.getLinks());
        }

        return c;
    }

}
