package eu.opertusmundi.common.config;

import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import eu.opertusmundi.common.model.sinergise.client.SentinelHubDeployment;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "opertusmundi.sentinel-hub", ignoreUnknownFields = true)
@PropertySource(value = "${opertusmundi.sentinel-hub.config}", ignoreResourceNotFound = true)
@Getter
@Setter
@Validated
public class SentinelHubConfiguration {

    @Valid
    private List<SentinelHubDeployment> deployments = Collections.emptyList();

    public SentinelHubDeployment getDeploymentByCollection(String id) {
        return this.getDeployments().stream()
            .filter(d -> d.getCollections().stream().anyMatch(c -> c.getId().equals(id)))
            .findFirst()
            .orElse(null);
    }
}
