package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;

import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class CatalogueItemDraftDto extends CatalogueItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public CatalogueItemDraftDto(CatalogueFeature feature) {
        super(feature);

        this.status = EnumDraftStatus.fromValue(feature.getProperties().getStatus());
    }

    @Getter
    @Setter
    private EnumDraftStatus status;

}
