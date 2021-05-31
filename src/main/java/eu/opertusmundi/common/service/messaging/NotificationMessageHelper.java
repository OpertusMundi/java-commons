package eu.opertusmundi.common.service.messaging;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.message.EnumNotificationType;

public interface NotificationMessageHelper {

    /**
     * Compose notification message
     * 
     * @param type
     * @param variables
     * @return
     */
    String composeNotificationText(EnumNotificationType type, Map<String, Object> variables) throws ServiceException;

    /**
     * Collect notification data
     * 
     * @param type
     * @param variables
     * @return
     */
    JsonNode collectNotificationData(EnumNotificationType type, Map<String, Object> variables) throws ServiceException;

}
