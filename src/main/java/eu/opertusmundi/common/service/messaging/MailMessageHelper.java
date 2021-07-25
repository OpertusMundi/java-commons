package eu.opertusmundi.common.service.messaging;

import java.util.Map;

import eu.opertusmundi.common.model.email.EmailAddressDto;
import eu.opertusmundi.common.model.email.EnumMailType;

public interface MailMessageHelper {

    /**
     * Get mail sender
     *
     * @param type
     * @param variables
     * @return
     */
    EmailAddressDto getSender(EnumMailType type, Map<String, Object> variables);

    /**
     * Compose mail subject
     *
     * @param type
     * @param variables
     * @return
     */
    String composeSubject(EnumMailType type, Map<String, Object> variables);

    /**
     * Resolve mail template
     *
     * @param type
     * @param variables
     * @return
     */
    String resolveTemplate(EnumMailType type, Map<String, Object> variables);

    /**
     * Create mail model
     *
     * @param type
     * @param builder
     * @param variables
     * @return
     */
    Map<String, Object> createModel(EnumMailType type, MailModelBuilder builder);

}
