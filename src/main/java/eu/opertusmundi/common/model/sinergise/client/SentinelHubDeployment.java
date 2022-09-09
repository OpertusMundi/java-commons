package eu.opertusmundi.common.model.sinergise.client;

import java.util.Collections;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
public class SentinelHubDeployment {

    @NotEmpty
    private String name;

    @NotEmpty
    private String url;

    @Valid
    private List<SentinelHubOpenDataCollection> collections = Collections.emptyList();

}