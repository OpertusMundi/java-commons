package eu.opertusmundi.common.model.catalogue.server;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public final class CatalogueFeature {

    public CatalogueFeature(CatalogueItemCommandDto command) {
        this.id       = command.getDraftKey() == null ? "" : command.getDraftKey().toString();
        this.type     = "Feature";
        this.geometry = command.getGeometry();

        this.properties = new CatalogueFeatureProperties(command);
    }

    String id;

    String type;

    @JsonInclude(Include.NON_NULL)
    Geometry geometry;

    CatalogueFeatureProperties properties;

}
