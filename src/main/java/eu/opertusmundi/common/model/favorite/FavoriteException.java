package eu.opertusmundi.common.model.favorite;

import eu.opertusmundi.common.model.ServiceException;

public final class FavoriteException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public FavoriteException(FavoriteMessageCode code) {
        super(code, "An unhandled exception has occurred");
    }

    public FavoriteException(FavoriteMessageCode code, String message) {
        super(code, message);
    }

    public FavoriteException(FavoriteMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
