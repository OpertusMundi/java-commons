package eu.opertusmundi.common.model.pricing;

import eu.opertusmundi.common.model.ServiceException;

public final class QuotationException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public QuotationException(QuotationMessageCode code) {
        super(code, "An unhandled exception has occurred");
    }

    public QuotationException(String message) {
        super(QuotationMessageCode.ERROR, "An unhandled exception has occurred");
    }

    public QuotationException(QuotationMessageCode code, String message) {
        super(code, message);
    }

    public QuotationException(String message, Throwable cause) {
        this(QuotationMessageCode.ERROR, message, cause);
    }

    public QuotationException(QuotationMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
