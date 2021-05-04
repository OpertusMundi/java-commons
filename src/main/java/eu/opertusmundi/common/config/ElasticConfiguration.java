package eu.opertusmundi.common.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import eu.opertusmundi.common.model.catalogue.elastic.HttpHostConfig;
import eu.opertusmundi.common.model.catalogue.elastic.IndexDefinition;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "opertusmundi.elastic")
public class ElasticConfiguration {

    @Getter
    @Setter
    private IndexDefinition assetIndex;

    @Getter
    @Setter
    private HttpHostConfig[] hosts;

    public List<IndexDefinition> getIndices() {
        return Arrays.asList(assetIndex);
    }
}
