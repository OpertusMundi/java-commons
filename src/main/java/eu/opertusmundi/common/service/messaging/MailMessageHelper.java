package eu.opertusmundi.common.service.messaging;

import java.util.Map;

import javax.annotation.Nullable;

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
     * @param variables
     * @return
     */
    default Map<String, Object> createModel(EnumMailType type) {
        return this.createModel(type, null);
    }

    /**
     * Create mail model
     * 
     * @param type
     * @param variables
     * @return
     */
    Map<String, Object> createModel(EnumMailType type, @Nullable Map<String, Object> variables);

}
