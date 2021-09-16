package eu.opertusmundi.common.model.favorite;

import eu.opertusmundi.common.model.MessageCode;

public enum FavoriteMessageCode implements MessageCode {
    CATALOGUE_ITEM_NOT_FOUND,
    FAVORITE_TYPE_NOT_SUPPORTED,
    PROVIDER_NOT_FOUND,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
