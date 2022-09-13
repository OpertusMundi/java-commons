package eu.opertusmundi.common.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "opertusmundi.geodata", ignoreUnknownFields = true)
@Getter
@Setter
@Validated
public class GeodataConfiguration {

    private List<String> shards;
}
