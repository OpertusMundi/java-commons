package eu.opertusmundi.common.service.messaging;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

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
import eu.opertusmundi.common.model.order.HelpdeskOrderDto;
import eu.opertusmundi.common.model.order.HelpdeskOrderItemDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskOrderPayInItemDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInDto;
import eu.opertusmundi.common.repository.NotificationTemplateRepository;
import eu.opertusmundi.common.repository.PayInRepository;
import eu.opertusmundi.common.service.CatalogueService;
import eu.opertusmundi.common.service.ProviderAssetService;
import io.jsonwebtoken.lang.Assert;

@Service
public class DefaultNotificationMessageHelper implements NotificationMessageHelper {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationTemplateRepository templateRepository;

    @Autowired
    private PayInRepository payInRepository;

    @Autowired
    private ProviderAssetService providerAssetService;

    @Autowired
    private CatalogueService catalogueService;

    @Override
    public String composeNotificationText(EnumNotificationType type, JsonNode data) throws ServiceException {
        Assert.isTrue(type != null, "Expected a non-null notification type");

        final NotificationTemplateEntity template = templateRepository.findOneByType(type).orElse(null);
        if (template == null) {
            throw new ServiceException(NotificationMessageCode.TEMPLATE_NOT_FOUND, String.format("Notification template %s was not found", type));
        }

        switch (type) {
            case CATALOGUE_ASSET_UNPUBLISHED :
            case CATALOGUE_HARVEST_COMPLETED :
            case ORDER_CONFIRMATION :
            case DELIVERY_REQUEST :
            case DIGITAL_DELIVERY :
            case PURCHASE_REMINDER :
            case DIGITAL_DELIVERY_BY_SUPPLIER :
            case PHYSICAL_DELIVERY_BY_SUPPLIER :
            case DIGITAL_DELIVERY_BY_PLATFORM :
            case PURCHASE_APPROVED :
            case PURCHASE_REJECTED :
            case FILES_UPLOAD_COMPLETED :
            case ASSET_PUBLISHING_ACCEPTED :
            case ASSET_PUBLISHING_REJECTED :
            case ASSET_PUBLISHED :
                return MessageFormat.format(template.getText(), this.jsonToMap(data));
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
            	return populateOrderConfirmationModel(variables, data);

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
            	return populateDigitalDeliveryBySupplierModel(variables, data);

            case PHYSICAL_DELIVERY_BY_SUPPLIER :
            	return populatePhysicalDeliveryBySupplierModel(variables, data);

            case DIGITAL_DELIVERY_BY_PLATFORM :
            	return populateDigitalDeliveryByPlatformModel(variables, data);

            case PURCHASE_APPROVED :
            	return populatePurchaseApprovedBySupplierModel(variables, data);

            case PURCHASE_REJECTED :
            	return populatePurchaseRejectedBySupplierModel(variables, data);

            case FILES_UPLOAD_COMPLETED :
            	// No parameters needed
            	return data;

            case ASSET_PUBLISHING_ACCEPTED :
            	return populatePublishingAcceptedModel(variables, data);

            case ASSET_PUBLISHING_REJECTED :
            	return populatePublishingRejectedModel(variables, data);

            case ASSET_PUBLISHED :
            	return populateAssetPublishedModel(variables, data);
        }

        // No variables required
        return null;
    }

	private ObjectNode populateOrderConfirmationModel(Map<String, Object> variables, ObjectNode data) {
        final UUID                      payInKey       = UUID.fromString((String) variables.get("payInKey"));
        final HelpdeskPayInDto          helpDeskPayIn  = this.payInRepository.findOneObjectByKey(payInKey).get();
        final HelpdeskOrderPayInItemDto payInOrderItem = (HelpdeskOrderPayInItemDto) helpDeskPayIn.getItems().get(0);
        final HelpdeskOrderDto          order          = payInOrderItem.getOrder();

    	data.put("orderId", order.getReferenceNumber());
        return data;
	}

	private ObjectNode populatePurchaseApprovedBySupplierModel(Map<String, Object> variables, ObjectNode data) {
        final UUID                      payInKey       = UUID.fromString((String) variables.get("payInKey"));
        final HelpdeskPayInDto          helpDeskPayIn  = this.payInRepository.findOneObjectByKey(payInKey).get();
        final HelpdeskOrderPayInItemDto payInOrderItem = (HelpdeskOrderPayInItemDto) helpDeskPayIn.getItems().get(0);
        final HelpdeskOrderItemDto      orderItem      = payInOrderItem.getOrder().getItems().get(0);

    	data.put("assetName", orderItem.getDescription());
    	data.put("assetVersion", orderItem.getAssetVersion());
    	data.put("supplierName", orderItem.getProvider().getName());
        return data;
	}

	private ObjectNode populatePurchaseRejectedBySupplierModel(Map<String, Object> variables, ObjectNode data) {
        final UUID                      payInKey       = UUID.fromString((String) variables.get("payInKey"));
        final HelpdeskPayInDto          helpDeskPayIn  = this.payInRepository.findOneObjectByKey(payInKey).get();
        final HelpdeskOrderPayInItemDto payInOrderItem = (HelpdeskOrderPayInItemDto) helpDeskPayIn.getItems().get(0);
        final HelpdeskOrderItemDto      orderItem      = payInOrderItem.getOrder().getItems().get(0);

    	data.put("assetName", orderItem.getDescription());
    	data.put("assetVersion", orderItem.getAssetVersion());
    	data.put("supplierName", orderItem.getProvider().getName());
        return data;
	}

	private ObjectNode populateDigitalDeliveryBySupplierModel(Map<String, Object> variables, ObjectNode data) {
        final UUID                      payInKey       = UUID.fromString((String) variables.get("payInKey"));
        final HelpdeskPayInDto          helpDeskPayIn  = this.payInRepository.findOneObjectByKey(payInKey).get();
        final HelpdeskOrderPayInItemDto payInOrderItem = (HelpdeskOrderPayInItemDto) helpDeskPayIn.getItems().get(0);
        final HelpdeskOrderItemDto      orderItem      = payInOrderItem.getOrder().getItems().get(0);

    	data.put("assetName", orderItem.getDescription());
    	data.put("assetVersion", orderItem.getAssetVersion());
        return data;
	}

	private ObjectNode populatePhysicalDeliveryBySupplierModel(Map<String, Object> variables, ObjectNode data) {
        final UUID                      payInKey       = UUID.fromString((String) variables.get("payInKey"));
        final HelpdeskPayInDto          helpDeskPayIn  = this.payInRepository.findOneObjectByKey(payInKey).get();
        final HelpdeskOrderPayInItemDto payInOrderItem = (HelpdeskOrderPayInItemDto) helpDeskPayIn.getItems().get(0);
        final HelpdeskOrderItemDto      orderItem      = payInOrderItem.getOrder().getItems().get(0);

    	data.put("assetName", orderItem.getDescription());
    	data.put("assetVersion", orderItem.getAssetVersion());
        return data;
	}

	private ObjectNode populateDigitalDeliveryByPlatformModel(Map<String, Object> variables, ObjectNode data) {
        final UUID                      payInKey       = UUID.fromString((String) variables.get("payInKey"));
        final HelpdeskPayInDto          helpDeskPayIn  = this.payInRepository.findOneObjectByKey(payInKey).get();
        final HelpdeskOrderPayInItemDto payInOrderItem = (HelpdeskOrderPayInItemDto) helpDeskPayIn.getItems().get(0);
        final HelpdeskOrderItemDto      orderItem      = payInOrderItem.getOrder().getItems().get(0);

    	data.put("assetName", orderItem.getDescription());
    	data.put("assetVersion", orderItem.getAssetVersion());
        return data;
	}

	private ObjectNode populatePublishingAcceptedModel(Map<String, Object> variables, ObjectNode data) {
        final UUID 		draftKey  = UUID.fromString((String) variables.get("draftKey"));
        final String 	assetName = this.providerAssetService.findOneDraft(draftKey).getTitle();

        data.put("assetName", assetName);
        return data;
	}

	private ObjectNode populatePublishingRejectedModel(Map<String, Object> variables, ObjectNode data) {
        final UUID 		draftKey  = UUID.fromString((String) variables.get("draftKey"));
        final String 	assetName = this.providerAssetService.findOneDraft(draftKey).getTitle();

        data.put("assetName", assetName);
        return data;
	}

	private ObjectNode populateAssetPublishedModel(Map<String, Object> variables, ObjectNode data) {
		final UUID 		draftKey  			= UUID.fromString((String) variables.get("draftKey"));
		final String 	assetPublishedId	= this.providerAssetService.findOneDraft(draftKey).getAssetPublished();
		final String 	assetName			= this.catalogueService.findOneFeature(assetPublishedId).getProperties().getTitle();

        data.put("assetName", assetName);
        return data;
	}

    private String checkAndGetVariable(Map<String, Object> variables, String name) {
        if (!variables.containsKey(name)) {
            throw new ServiceException(NotificationMessageCode.VARIABLE_MISSING, String.format("Variable %s not found", name));
        }

        return (String) variables.get(name);
    }

    private Map<String, Object> jsonToMap(JsonNode data) {
        final Map<String, Object> result = new HashMap<>();

        final ObjectNode                            node = (ObjectNode) data;
        final Iterator<Map.Entry<String, JsonNode>> iter = node.fields();

        while (iter.hasNext()) {
            final Map.Entry<String, JsonNode> entry = iter.next();
            if (!entry.getValue().isNull()) {
                result.put(entry.getKey(), entry.getValue().asText());
            }
        }

        return result;
    }

}
