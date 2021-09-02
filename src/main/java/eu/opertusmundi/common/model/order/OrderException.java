package eu.opertusmundi.common.model.order;

import eu.opertusmundi.common.model.ServiceException;

public final class OrderException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public OrderException(OrderMessageCode code) {
        super(code, "An unhandled exception has occurred");
    }

    public OrderException(OrderMessageCode code, String message) {
        super(code, message);
    }

    public OrderException(OrderMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
