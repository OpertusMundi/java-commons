package eu.opertusmundi.common.model.email;

import eu.opertusmundi.common.model.MessageCode;

public enum MailMessageCode implements MessageCode {
    TYPE_NOT_SUPPORTED,
    RECIPIENT_NOT_FOUND,
    TEMPLATE_NOT_FOUND,
    SEND_MAIL_FAILED,
    SERVICE_ERROR,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
