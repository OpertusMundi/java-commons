package eu.opertusmundi.common.model.catalogue.server;

import org.locationtech.jts.geom.Geometry;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CatalogueFeature {

    public CatalogueFeature(CatalogueItemCommandDto command) {
        this.id       = command.getAssetKey() == null ? "" : command.getAssetKey().toString();
        this.type     = "Feature";
        this.geometry = command.getGeometry();

        this.properties = new CatalogueFeatureProperties(command);
    }

    String id;

    String type;

    Geometry geometry;

    CatalogueFeatureProperties properties;

}
