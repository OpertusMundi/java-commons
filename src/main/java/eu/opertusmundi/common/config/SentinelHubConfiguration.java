package eu.opertusmundi.common.config;

import java.util.Collections;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "opertusmundi.sentinel-hub", ignoreUnknownFields = true)
@PropertySource(value = "classpath:config/sentinel-hub.properties", ignoreResourceNotFound = true)
@Getter
@Setter
@Validated
public class SentinelHubConfiguration {

    @Valid
    private List<Deployment> deployments = Collections.emptyList();

    @Getter
    @Setter
    public static class Deployment {

        @NotEmpty
        private String name;

        @NotEmpty
        private String url;

        private List<String> collections = Collections.emptyList();

    }

    public Deployment getDeploymentByCollection(String name) {
        return this.getDeployments().stream()
            .filter(d -> d.collections.contains(name))
            .findFirst()
            .orElse(null);
    }
}
