package eu.opertusmundi.common.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import eu.opertusmundi.common.model.catalogue.elastic.HttpHostConfig;
import eu.opertusmundi.common.model.catalogue.elastic.IndexDefinition;
import eu.opertusmundi.common.model.catalogue.elastic.TransformDefinition;
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
    private IndexDefinition assetViewIndex;

    @Getter
    @Setter
    private IndexDefinition assetViewAggregateIndex;

    @Getter
    @Setter
    private IndexDefinition profileIndex;

    @Getter
    @Setter
    private TransformDefinition assetViewAggregateTransform;

    @Getter
    @Setter
    private HttpHostConfig[] hosts;

    public List<IndexDefinition> getIndices() {
        return Arrays.asList(
            assetIndex,
            assetViewIndex,
            assetViewAggregateIndex,
            profileIndex
        );
    }

}
