package eu.opertusmundi.common.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.util.Assert;

public class ApplicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * An error code that generally categorized this application-level
     * exception.
     */
    private final MessageCode code;

    /**
     * A message pattern for this exception (to be used when formatting with
     * context variables)
     */
    private String pattern;

    /**
     * A list of variables holding the context of this exception
     */
    private List<Object> vars;

    /**
     * A pre-formatted message for this exception
     */
    private String message;

    private ApplicationException(Throwable cause, MessageCode code) {
        super(cause);
        this.code = code;
    }

    public static ApplicationException fromMessage(String message) {
        return fromMessage(null, BasicMessageCode.InternalServerError, message);
    }

    public static ApplicationException fromMessage(MessageCode code, String message) {
        return fromMessage(null, code, message);
    }

    public static ApplicationException fromMessage(Throwable cause, String message) {
        return fromMessage(cause, BasicMessageCode.InternalServerError, message);
    }

    public static ApplicationException fromMessage(Throwable cause, MessageCode code, String message) {
        final ApplicationException e = new ApplicationException(cause, code);
        Assert.notNull(message, "Expected a non-null message");
        e.message = message;
        e.pattern = null;
        return e;
    }

    public static ApplicationException fromPattern(String pattern) {
        return fromPattern(null, BasicMessageCode.InternalServerError, pattern, Collections.emptyList());
    }

    public static ApplicationException fromPattern(MessageCode code, String pattern) {
        return fromPattern(null, code, pattern, Collections.emptyList());
    }

    public static ApplicationException fromPattern(MessageCode code) {
        return fromPattern(null, code, null, Collections.emptyList());
    }

    public static ApplicationException fromPattern(MessageCode code, String pattern, List<?> vars) {
        return fromPattern(null, code, pattern, vars);
    }

    public static ApplicationException fromPattern(MessageCode code, List<?> vars) {
        return fromPattern(null, code, null, vars);
    }

    public static ApplicationException fromPattern(Throwable cause, String pattern) {
        return fromPattern(cause, BasicMessageCode.InternalServerError, pattern, Collections.emptyList());
    }

    public static ApplicationException fromPattern(Throwable cause, MessageCode code, String pattern) {
        return fromPattern(cause, code, pattern, Collections.emptyList());
    }

    public static ApplicationException fromPattern(Throwable cause, MessageCode code) {
        return fromPattern(cause, code, null, Collections.emptyList());
    }

    public static ApplicationException fromPattern(Throwable cause, MessageCode code, List<?> vars) {
        return fromPattern(cause, code, null, vars);
    }

    public static ApplicationException fromPattern(Throwable cause, MessageCode code, String pattern, List<?> vars) {
        final ApplicationException e = new ApplicationException(cause, code);
        Assert.isTrue(pattern != null || code != null && !code.key().isEmpty(),
                "Expected a non-null pattern or a non-empty error code");
        e.message = null;
        e.pattern = pattern;
        e.vars = new ArrayList<>(vars);
        return e;
    }

    public MessageCode getErrorCode() {
        return this.code;
    }

    public List<Object> getContextVars() {
        return Collections.unmodifiableList(this.vars);
    }

    @Override
    public String getMessage() {
        if (this.message != null) {
            // Assume a pre-formatted message is present, return as-is
            return this.message;
        } else if (this.pattern != null) {
            // Format a message using pattern and context variables
            return MessageFormat.format(this.pattern, this.vars.toArray());
        } else {
            // Cannot format without a MessageSource, so just return key
            return this.code.key();
        }
    }

    /**
     * Return an instance of an {@link ApplicationException} (of same error code) by
     * formatting the message with the assist of a given {@link MessageSource}.
     *
     * <p>
     * Note: In case <tt>this</tt> is already formatted, it is returned as-is.
     * Otherwise, a new instance is created and returned. In any case, <tt>this</tt>
     * remains unchanged.
     *
     * @param messageSource
     * @param locale
     * @return
     */
    public ApplicationException withFormattedMessage(MessageSource messageSource, Locale locale) {
        if (this.message == null && this.pattern == null) {
            // Resolve pattern by code; create new exception with formatted message
            final String message = messageSource.getMessage(this.code.key(), this.vars.toArray(), locale);
            return fromMessage(this.getCause(), this.code, message);
        } else {
            // Either a message or an pattern is present: no need to use MessageSource
            return this;
        }
    }

    /**
     * Return an instance of an {@link Message} (of same error code) by formatting the
     * message with the assist of a given {@link MessageSource}.
     *
     * @param messageSource
     * @param locale
     */
    public Message toError(MessageSource messageSource, Locale locale) {
        return new Message(this.code, this.withFormattedMessage(messageSource, locale).getMessage());
    }

    /**
     * Return an instance of an {@link Message} (of same error code) with the existing
     * message (no formatting takes place).
     */
    public Message toError() {
        return new Message(this.code, this.message);
    }
}
