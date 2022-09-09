package eu.opertusmundi.common.model.sinergise.client;

import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SentinelHubOpenDataCollection {

    @NotEmpty
    private String id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String instanceId;

}
