package eu.opertusmundi.common.model.discovery.server;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerJoinableTableDto {

    private List<ServerJoinableTableMatchDto> matches;

    @JsonProperty("table_name")
    private String tableName;

}
