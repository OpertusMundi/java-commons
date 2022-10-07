package eu.opertusmundi.common.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import eu.opertusmundi.common.model.geodata.Shard;
import eu.opertusmundi.common.util.StreamUtils;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "opertusmundi.geodata", ignoreUnknownFields = true)
@PropertySource(value = "${opertusmundi.geodata.config:config/geodata.properties}", ignoreResourceNotFound = true)
@Getter
@Setter
@Validated
public class GeodataConfiguration {

    private List<Shard> shards;

    public Shard getShardById(String id) {
        return StreamUtils.from(this.shards)
            .filter(s -> s.getId().equalsIgnoreCase(id))
            .findFirst()
            .orElse(null);
    }
}
