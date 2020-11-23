package eu.opertusmundi.common.model.catalogue.client;

import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import lombok.Getter;
import lombok.Setter;

public class CatalogueItemDraftDetailsDto extends CatalogueItemDetailsDto {

    public CatalogueItemDraftDetailsDto(CatalogueFeature feature) {
        super(feature);

        this.status = EnumDraftStatus.fromValue(feature.getProperties().getStatus());
    }

    @Getter
    @Setter
    private EnumDraftStatus status;

}
