package eu.opertusmundi.common.service.messaging;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ibm.icu.text.MessageFormat;

import eu.opertusmundi.common.domain.NotificationTemplateEntity;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.message.EnumNotificationType;
import eu.opertusmundi.common.model.message.client.NotificationMessageCode;
import eu.opertusmundi.common.repository.NotificationTemplateRepository;
import io.jsonwebtoken.lang.Assert;

@Service
public class DefaultNotificationMessageHelper implements NotificationMessageHelper {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationTemplateRepository templateRepository;
       
    @Override
    public String composeNotificationText(EnumNotificationType type, Map<String, Object> variables) throws ServiceException {
        Assert.isTrue(type != null, "Expected a non-null notification type");

        final NotificationTemplateEntity template = templateRepository.findOneByType(type).orElse(null);
        if (template == null) {
            throw new ServiceException(NotificationMessageCode.TEMPLATE_NOT_FOUND, String.format("Notification template %s was not found", type));
        }

        switch (type) {
            case CATALOGUE_HARVEST_COMPLETED :
                return MessageFormat.format(template.getText(), variables);
        }

        throw new ServiceException(NotificationMessageCode.TYPE_NOT_SUPPORTED, String.format("Notification type %s is not supported", type));
    }

    @Override
    public JsonNode collectNotificationData(EnumNotificationType type, Map<String, Object> variables) {
        Assert.isTrue(type != null, "Expected a non-null notification type");

        switch (type) {
            case CATALOGUE_HARVEST_COMPLETED :
                final ObjectNode data = objectMapper.createObjectNode();
                data.put("catalogueUrl", this.checkAndGetVariable(variables, "catalogueUrl"));
                data.put("catalogueType", this.checkAndGetVariable(variables, "catalogueType"));
                return data;
        }

        // No variables required
        return null;
    }

    private String checkAndGetVariable(Map<String, Object> variables, String name) {
        if (!variables.containsKey(name)) {
            throw new ServiceException(NotificationMessageCode.VARIABLE_MISSING, String.format("Variable %s not found", name));
        }

        return (String) variables.get(name);
    }

}
