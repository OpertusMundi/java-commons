package eu.opertusmundi.common.model.discovery.server;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerJoinableTableMatchDto {

    @JsonProperty("PK")
    private ServerJoinableTableMatchKeysDto keys;

    @JsonProperty("RELATED")
    private ServerJoinableTableMatchRelatedDto related;

    private String explanation;

}
