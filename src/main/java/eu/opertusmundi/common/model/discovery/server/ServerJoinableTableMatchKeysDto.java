package eu.opertusmundi.common.model.discovery.server;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerJoinableTableMatchKeysDto {

    @JsonProperty("from_id")
    private String from;

    @JsonProperty("to_id")
    private String to;

}