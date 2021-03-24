package eu.opertusmundi.common.model.order;

import eu.opertusmundi.common.model.ServiceException;

public final class CartException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public CartException(CartMessageCode code) {
        super(code, "An unhandled exception has occurred");
    }

    public CartException(String message) {
        super(CartMessageCode.ERROR, "An unhandled exception has occurred");
    }

    public CartException(CartMessageCode code, String message) {
        super(code, message);
    }

    public CartException(String message, Throwable cause) {
        this(CartMessageCode.ERROR, message, cause);
    }

    public CartException(CartMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
