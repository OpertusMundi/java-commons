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
            case CATALOGUE_ASSET_UNPUBLISHED :
            case CATALOGUE_HARVEST_COMPLETED :
            case ORDER_CONFIRMATION:
            case DELIVERY_REQUEST:
            case DIGITAL_DELIVERY:
            case PURCHASE_REMINDER:
            case DIGITAL_DELIVERY_BY_SUPPLIER:
            case PHYSICAL_DELIVERY_BY_SUPPLIER:
            case DIGITAL_DELIVERY_BY_PLATFORM:
            case PURCHASE_REJECTED:
            case FILES_UPLOAD_COMPLETED:
            case ASSET_PUBLISHING_ACCEPTED:
            case ASSET_PUBLISHING_REJECTED:
                return MessageFormat.format(template.getText(), variables);
        }

        throw new ServiceException(NotificationMessageCode.TYPE_NOT_SUPPORTED, String.format("Notification type %s is not supported", type));
    }

    @Override
    public JsonNode collectNotificationData(EnumNotificationType type, Map<String, Object> variables) {
        Assert.isTrue(type != null, "Expected a non-null notification type");

        final ObjectNode data = objectMapper.createObjectNode();

        switch (type) {
            case CATALOGUE_ASSET_UNPUBLISHED :
                data.put("assetName", this.checkAndGetVariable(variables, "assetName"));
                data.put("assetVersion", this.checkAndGetVariable(variables, "assetVersion"));
                return data;

            case CATALOGUE_HARVEST_COMPLETED :
                data.put("catalogueUrl", this.checkAndGetVariable(variables, "catalogueUrl"));
                data.put("catalogueType", this.checkAndGetVariable(variables, "catalogueType"));
                return data;
                
            case ORDER_CONFIRMATION :
            	data.put("orderId", this.checkAndGetVariable(variables, "orderId"));
            	return data;
            	
            case DELIVERY_REQUEST :
            	data.put("assetName", this.checkAndGetVariable(variables, "assetName"));
            	data.put("assetVersion", this.checkAndGetVariable(variables, "assetVersion"));
            	return data;
            	
            case DIGITAL_DELIVERY :
            	data.put("assetName", this.checkAndGetVariable(variables, "assetName"));
            	data.put("assetVersion", this.checkAndGetVariable(variables, "assetVersion"));
            	return data;
            	
            case PURCHASE_REMINDER :
            	data.put("assetName", this.checkAndGetVariable(variables, "assetName"));
            	data.put("assetVersion", this.checkAndGetVariable(variables, "assetVersion"));
            	return data;
            	
            case DIGITAL_DELIVERY_BY_SUPPLIER :
            	data.put("assetName", this.checkAndGetVariable(variables, "assetName"));
            	data.put("assetVersion", this.checkAndGetVariable(variables, "assetVersion"));
            	return data;
            	
            case PHYSICAL_DELIVERY_BY_SUPPLIER :
            	data.put("assetName", this.checkAndGetVariable(variables, "assetName"));
            	data.put("assetVersion", this.checkAndGetVariable(variables, "assetVersion"));
            	return data;
            	
            case DIGITAL_DELIVERY_BY_PLATFORM :
            	data.put("assetName", this.checkAndGetVariable(variables, "assetName"));
            	data.put("assetVersion", this.checkAndGetVariable(variables, "assetVersion"));
            	return data;
            	
            case PURCHASE_REJECTED :
            	data.put("assetName", this.checkAndGetVariable(variables, "assetName"));
            	data.put("assetVersion", this.checkAndGetVariable(variables, "assetVersion"));
            	data.put("supplierName", this.checkAndGetVariable(variables, "supplierName"));
            	return data;
            	
            case FILES_UPLOAD_COMPLETED :
            	return data;
            	
            case ASSET_PUBLISHING_ACCEPTED :
            	data.put("assetName", this.checkAndGetVariable(variables, "assetName"));
            	return data;
            	
            case ASSET_PUBLISHING_REJECTED :
            	data.put("assetName", this.checkAndGetVariable(variables, "assetName"));
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
