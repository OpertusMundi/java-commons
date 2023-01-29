package eu.opertusmundi.common.model.discovery.server;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerRelatedTableResultDto {

    @JsonProperty("RelatedTables")
    private List<ServerRelatedTableDto> relatedTables;

}
