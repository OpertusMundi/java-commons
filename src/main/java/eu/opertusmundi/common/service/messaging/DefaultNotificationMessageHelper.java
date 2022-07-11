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
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeatureProperties;
import eu.opertusmundi.common.model.message.EnumNotificationType;
import eu.opertusmundi.common.model.message.client.NotificationMessageCode;
import eu.opertusmundi.common.model.order.HelpdeskOrderDto;
import eu.opertusmundi.common.model.order.HelpdeskOrderItemDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskOrderPayInItemDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInDto;
import eu.opertusmundi.common.repository.NotificationTemplateRepository;
import eu.opertusmundi.common.repository.OrderRepository;
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
    private OrderRepository orderRepository;

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
            case ASSET_PUBLISHING_CANCELLED :
            case ASSET_PUBLISHED :
            case COPY_FILE_TO_TOPIO_DRIVE_SUCCESS :
            case COPY_FILE_TO_TOPIO_DRIVE_ERROR :
            case USER_SERVICE_PUBLISH_SUCCESS :
            case USER_SERVICE_PUBLISH_FAILURE :
            case USER_SERVICE_REMOVE :
                return MessageFormat.format(template.getText(), this.jsonToMap(data));
        }

        throw new ServiceException(NotificationMessageCode.TYPE_NOT_SUPPORTED, String.format("Notification type %s is not supported", type));
    }

    @Override
    public JsonNode collectNotificationData(EnumNotificationType type, Map<String, Object> variables) {
        Assert.isTrue(type != null, "Expected a non-null notification type");

        final ObjectNode data = objectMapper.createObjectNode();
        data.put("type", type.toString());

        switch (type) {
            case CATALOGUE_ASSET_UNPUBLISHED :
                data.put("assetId", this.checkAndGetVariable(variables, "assetId"));
                data.put("assetName", this.checkAndGetVariable(variables, "assetName"));
                data.put("assetVersion", this.checkAndGetVariable(variables, "assetVersion"));
                data.put("publisherKey", this.checkAndGetVariable(variables, "publisherKey"));
                return data;

            case CATALOGUE_HARVEST_COMPLETED :
                data.put("catalogueUrl", this.checkAndGetVariable(variables, "catalogueUrl"));
                data.put("catalogueType", this.checkAndGetVariable(variables, "catalogueType"));
                return data;

            case ORDER_CONFIRMATION :
                return populateOrderConfirmationModel(variables, data);

            case DELIVERY_REQUEST :
                data.put("orderKey", this.checkAndGetVariable(variables, "orderKey"));
                data.put("assetName", this.checkAndGetVariable(variables, "assetName"));
                return data;

            case DIGITAL_DELIVERY :
                data.put("orderKey", this.checkAndGetVariable(variables, "orderKey"));
                data.put("assetName", this.checkAndGetVariable(variables, "assetName"));
                data.put("assetVersion", this.checkAndGetVariable(variables, "assetVersion"));
                return data;

            case PURCHASE_REMINDER :
                data.put("orderKey", this.checkAndGetVariable(variables, "orderKey"));
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
            case PURCHASE_REJECTED :
                return populatePurchaseApprovedBySupplierModel(variables, data);

            case FILES_UPLOAD_COMPLETED :
                // No parameters needed
                return data;

            case ASSET_PUBLISHING_ACCEPTED :
                return populatePublishingAcceptedModel(variables, data);

            case ASSET_PUBLISHING_REJECTED :
                return populatePublishingRejectedModel(variables, data);

            case ASSET_PUBLISHING_CANCELLED:
                return populatePublishingCancelledModel(variables, data);

            case ASSET_PUBLISHED :
                return populateAssetPublishedModel(variables, data);

            case COPY_FILE_TO_TOPIO_DRIVE_SUCCESS:
            case COPY_FILE_TO_TOPIO_DRIVE_ERROR:
                data.put("assetId", this.checkAndGetVariable(variables, "assetId"));
                data.put("assetName", this.checkAndGetVariable(variables, "assetName"));
                data.put("assetVersion", this.checkAndGetVariable(variables, "assetVersion"));
                data.put("resourceKey", this.checkAndGetVariable(variables, "resourceKey"));
                data.put("resourceFileName", this.checkAndGetVariable(variables, "resourceFileName"));
                return data;

            case USER_SERVICE_PUBLISH_SUCCESS :
            case USER_SERVICE_PUBLISH_FAILURE :
            case USER_SERVICE_REMOVE :
                data.put("serviceKey", this.checkAndGetVariable(variables, "serviceKey"));
                data.put("serviceTitle", this.checkAndGetVariable(variables, "serviceTitle"));
                data.put("serviceVersion", this.checkAndGetVariable(variables, "serviceVersion"));
                return data;
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
        data.put("orderKey", order.getKey().toString());
        data.put("payInKey", payInKey.toString());
        return data;
    }

    private ObjectNode populatePurchaseApprovedBySupplierModel(Map<String, Object> variables, ObjectNode data) {
        final UUID            orderKey        = UUID.fromString((String) variables.get("orderKey"));
        final OrderEntity     orderEntity     = orderRepository.findOrderEntityByKey(orderKey).get();
        final OrderItemEntity orderItemEntity = orderEntity.getItems().get(0);

        data.put("orderId", orderEntity.getReferenceNumber());
        data.put("orderKey", orderEntity.getKey().toString());
        data.put("assetId", orderItemEntity.getAssetId());
        data.put("assetName", orderItemEntity.getDescription());
        data.put("assetVersion", orderItemEntity.getAssetVersion());
        data.put("supplierName", orderItemEntity.getProvider().getProvider().getName());
        return data;
    }

    private ObjectNode populateDigitalDeliveryBySupplierModel(Map<String, Object> variables, ObjectNode data) {
        final UUID                      payInKey       = UUID.fromString((String) variables.get("payInKey"));
        final HelpdeskPayInDto          helpDeskPayIn  = this.payInRepository.findOneObjectByKey(payInKey).get();
        final HelpdeskOrderPayInItemDto payInOrderItem = (HelpdeskOrderPayInItemDto) helpDeskPayIn.getItems().get(0);
        final HelpdeskOrderItemDto      orderItem      = payInOrderItem.getOrder().getItems().get(0);

        data.put("payInKey", payInKey.toString());
        data.put("orderKey", payInOrderItem.getOrder().getKey().toString());
        data.put("assetId", orderItem.getAssetId());
        data.put("assetName", orderItem.getDescription());
        data.put("assetVersion", orderItem.getAssetVersion());
        return data;
    }

    private ObjectNode populatePhysicalDeliveryBySupplierModel(Map<String, Object> variables, ObjectNode data) {
        final UUID                      payInKey       = UUID.fromString((String) variables.get("payInKey"));
        final HelpdeskPayInDto          helpDeskPayIn  = this.payInRepository.findOneObjectByKey(payInKey).get();
        final HelpdeskOrderPayInItemDto payInOrderItem = (HelpdeskOrderPayInItemDto) helpDeskPayIn.getItems().get(0);
        final HelpdeskOrderItemDto      orderItem      = payInOrderItem.getOrder().getItems().get(0);

        data.put("payInKey", payInKey.toString());
        data.put("orderKey", payInOrderItem.getOrder().getKey().toString());
        data.put("assetId", orderItem.getAssetId());
        data.put("assetName", orderItem.getDescription());
        data.put("assetVersion", orderItem.getAssetVersion());
        return data;
    }

    private ObjectNode populateDigitalDeliveryByPlatformModel(Map<String, Object> variables, ObjectNode data) {
        final UUID                      payInKey       = UUID.fromString((String) variables.get("payInKey"));
        final HelpdeskPayInDto          helpDeskPayIn  = this.payInRepository.findOneObjectByKey(payInKey).get();
        final HelpdeskOrderPayInItemDto payInOrderItem = (HelpdeskOrderPayInItemDto) helpDeskPayIn.getItems().get(0);
        final HelpdeskOrderItemDto      orderItem      = payInOrderItem.getOrder().getItems().get(0);

        data.put("payInKey", payInKey.toString());
        data.put("orderKey", payInOrderItem.getOrder().getKey().toString());
        data.put("assetId", orderItem.getAssetId());
        data.put("assetName", orderItem.getDescription());
        data.put("assetVersion", orderItem.getAssetVersion());
        return data;
    }

    private ObjectNode populatePublishingAcceptedModel(Map<String, Object> variables, ObjectNode data) {
        final UUID          draftKey = UUID.fromString((String) variables.get("draftKey"));
        final AssetDraftDto draft    = this.providerAssetService.findOneDraft(draftKey);

        data.put("draftKey", draftKey.toString());
        data.put("assetName", draft.getTitle());
        data.put("assetVersion", draft.getVersion());
        return data;
    }

    private ObjectNode populatePublishingRejectedModel(Map<String, Object> variables, ObjectNode data) {
        final UUID          draftKey = UUID.fromString((String) variables.get("draftKey"));
        final AssetDraftDto draft    = this.providerAssetService.findOneDraft(draftKey);

        data.put("draftKey", draftKey.toString());
        data.put("assetName", draft.getTitle());
        data.put("assetVersion", draft.getVersion());
        return data;
    }

    private ObjectNode populatePublishingCancelledModel(Map<String, Object> variables, ObjectNode data) {
        final UUID          draftKey = UUID.fromString((String) variables.get("draftKey"));
        final AssetDraftDto draft    = this.providerAssetService.findOneDraft(draftKey);

        Assert.notNull(draft, "Expected a non-null draft");

        final String assetName    = draft.getTitle();
        final String assetVersion = draft.getVersion();
        final String errorMessage = draft.getHelpdeskErrorMessage();

        data.put("draftKey", draftKey.toString());
        data.put("assetName", assetName);
        data.put("assetVersion", assetVersion);
        data.put("errorMessage", errorMessage);
        return data;
    }

    private ObjectNode populateAssetPublishedModel(Map<String, Object> variables, ObjectNode data) {
        final UUID                       draftKey         = UUID.fromString((String) variables.get("draftKey"));
        final String                     assetPublishedId = this.providerAssetService.findOneDraft(draftKey).getAssetPublished();
        final CatalogueFeatureProperties props            = this.catalogueService.findOneFeature(assetPublishedId).getProperties();

        data.put("draftKey", draftKey.toString());
        data.put("assetId", assetPublishedId);
        data.put("assetName", props.getTitle());
        data.put("assetVersion", props.getVersion());
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
