package eu.opertusmundi.common.model.discovery.server;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerRelatedTableDto {

    private List<String> links;
    private String       explanation;

}
