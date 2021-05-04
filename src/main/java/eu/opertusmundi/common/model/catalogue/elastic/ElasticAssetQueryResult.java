package eu.opertusmundi.common.model.catalogue.elastic;

import java.util.List;

import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import lombok.Getter;
import lombok.Setter;

public class ElasticAssetQueryResult {

    @Getter
    @Setter
    private long total;

    @Getter
    @Setter
    private List<CatalogueFeature> assets;

}
