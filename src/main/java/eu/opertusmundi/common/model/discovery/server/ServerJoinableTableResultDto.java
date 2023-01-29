package eu.opertusmundi.common.model.discovery.server;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServerJoinableTableResultDto {

    @JsonProperty("JoinableTables")
    private List<ServerJoinableTableDto> joinableTables;

}
