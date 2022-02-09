package eu.opertusmundi.common.model.sinergise.client;

import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SentinelHubDeployment {

    @NotEmpty
    private String name;

    @NotEmpty
    private String url;

    private List<SentinelHubOpenDataCollection> collections = Collections.emptyList();

}