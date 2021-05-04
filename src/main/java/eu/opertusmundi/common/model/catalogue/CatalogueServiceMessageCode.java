package eu.opertusmundi.common.model.catalogue;

import eu.opertusmundi.common.model.MessageCode;

public enum CatalogueServiceMessageCode implements MessageCode {
    ERROR,
    CATALOGUE_SERVICE,
    PUBLISHER_NOT_FOUND,
    PRICING_MODEL,
    ITEM_NOT_FOUND,
    HARVEST_ITEM_NOT_FOUND,
    ELASTIC_SERVICE,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
