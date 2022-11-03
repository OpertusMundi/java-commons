package eu.opertusmundi.common.service.messaging;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.ibm.icu.text.MessageFormat;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.ActivationTokenEntity;
import eu.opertusmundi.common.domain.AddressEmbeddable;
import eu.opertusmundi.common.domain.CustomerIndividualEntity;
import eu.opertusmundi.common.domain.CustomerProfessionalEntity;
import eu.opertusmundi.common.domain.MailTemplateEntity;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.account.EnumActivationTokenType;
import eu.opertusmundi.common.model.account.EnumMangopayUserType;
import eu.opertusmundi.common.model.email.EmailAddressDto;
import eu.opertusmundi.common.model.email.EnumMailType;
import eu.opertusmundi.common.model.email.MailMessageCode;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.ActivationTokenRepository;
import eu.opertusmundi.common.repository.MailTemplateRepository;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.service.CatalogueService;
import eu.opertusmundi.common.service.ProviderAssetService;

@Service
public class DefaultMailMessageHelper implements MailMessageHelper {

    @Value("${opertus-mundi.base-url:}")
    private String baseUrl;

    @Autowired
    private MailTemplateRepository templateRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProviderAssetService providerAssetService;

    @Autowired
    private CatalogueService catalogueService;

    @Override
    public String composeSubject(EnumMailType type, Map<String, Object> variables) {
        Assert.notNull(type, "Expected a non-null mail type");

        final MailTemplateEntity template = templateRepository.findOneByType(type).orElse(null);
        if (template == null) {
            throw new ServiceException(MailMessageCode.TEMPLATE_NOT_FOUND, String.format("Mail template %s was not found", type));
        }

        switch (type) {
            case ACCOUNT_ACTIVATION_SUCCESS :
            case ACCOUNT_ACTIVATION_TOKEN :
            case ACCOUNT_ACTIVATION_WELCOME :
            case ACCOUNT_PASSWORD_RESET :
            case CATALOGUE_HARVEST_COMPLETED :
            case CONSUMER_DIGITAL_DELIVERY_BY_PLATFORM :
            case CONSUMER_DIGITAL_DELIVERY_BY_SUPPLIER :
            case CONSUMER_PHYSICAL_DELIVERY_BY_SUPPLIER :
            case CONSUMER_PURCHASE_APPROVED :
            case CONSUMER_PURCHASE_NOTIFICATION :
            case CONSUMER_PURCHASE_REJECTION :
            case CONSUMER_RECEIPT_ISSUED :
            case CONSUMER_INVOICE :
            case CONSUMER_FILLED_OUT_CONTRACT :
            case FILES_UPLOAD_COMPLETED :
            case MASTER_TEMPLATE_CONTRACT_UPDATE :
            case ORDER_CONFIRMATION :
            case SUPPLIER_DELIVERY_REQUEST :
            case SUPPLIER_DIGITAL_DELIVERY :
            case SUPPLIER_EVALUATION :
            case SUPPLIER_PUBLISHING_ACCEPTED :
            case SUPPLIER_PUBLISHING_REJECTED :
            case SUPPLIER_ASSET_PUBLISHED :
            case SUPPLIER_PURCHASE_REMINDER :
            case SUPPLIER_UPLOADED_CONTRACT_EVALUATION :
            case SUPPLIER_UPLOADED_CONTRACT_ACCEPTED :
            case SUPPLIER_UPLOADED_CONTRACT_REJECTED:
            case SUPPLIER_CONTRACT_TO_BE_FILLED_OUT :
            case CONSUMER_SUPPLIER_CONTRACT_SIGNED :
            case VENDOR_ACCOUNT_INVITATION:
            case VENDOR_ACCOUNT_ACTIVATION_SUCCESS :
                return MessageFormat.format(template.getSubjectTemplate(), variables);
        }

        throw new ServiceException(MailMessageCode.TYPE_NOT_SUPPORTED, String.format("Mail type %s is not supported", type));
    }

    @Override
    public String resolveTemplate(EnumMailType type, Map<String, Object> variables) {
        Assert.notNull(type, "Expected a non-null mail type");

        final MailTemplateEntity template = templateRepository.findOneByType(type).orElse(null);
        if (template == null) {
            throw new ServiceException(MailMessageCode.TEMPLATE_NOT_FOUND, String.format("Mail template %s was not found", type));
        }

        return template.getContentTemplate();
    }

    @Override
    public EmailAddressDto getSender(EnumMailType type, Map<String, Object> variables) {
        Assert.notNull(type, "Expected a non-null mail type");

        final MailTemplateEntity template = templateRepository.findOneByType(type).orElse(null);
        if (template == null) {
            throw new ServiceException(MailMessageCode.TEMPLATE_NOT_FOUND, String.format("Mail template %s was not found", type));
        }

        return EmailAddressDto.of(template.getSenderEmail(), template.getSenderName());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> createModel(EnumMailType type, MailModelBuilder builder) {
        Assert.notNull(type, "Expected a non-null mail type");

        switch (type) {
            case ACCOUNT_ACTIVATION_TOKEN :
            case VENDOR_ACCOUNT_INVITATION:
                this.populateAccountActivationTokenModel(builder);
                break;

            case ACCOUNT_ACTIVATION_SUCCESS :
            case VENDOR_ACCOUNT_ACTIVATION_SUCCESS :
                this.populateAccountActivationSuccessModel(builder);
                break;

            case ACCOUNT_ACTIVATION_WELCOME :
            	this.populateAccountActivationWelcomeModel(builder);
            	break;

            case ACCOUNT_PASSWORD_RESET :
                this.populateAccountPasswordResetModel(builder);
                break;

            case ORDER_CONFIRMATION :
            	this.populateOrderConfirmationModel(builder);
            	break;

            case SUPPLIER_EVALUATION :
            	this.populateSupplierEvaluationModel(builder);
            	break;

            case SUPPLIER_DELIVERY_REQUEST :
            	this.populateSupplierDeliveryRequestModel(builder);
            	break;

            case SUPPLIER_DIGITAL_DELIVERY :
            	this.populateSupplierDigitalDeliveryModel(builder);
            	break;

            case SUPPLIER_PURCHASE_REMINDER :
            	this.populateSupplierPurchaseReminderModel(builder);
            	break;

            case SUPPLIER_PUBLISHING_ACCEPTED :
            	this.populateSupplierAssetAcceptedByHelpdeskModel(builder);
            	break;

            case SUPPLIER_PUBLISHING_REJECTED :
            	this.populateSupplierAssetRejectedByHelpdeskModel(builder);
            	break;

            case SUPPLIER_ASSET_PUBLISHED :
            	this.populateSupplierAssetPublishedModel(builder);
            	break;

    		case SUPPLIER_UPLOADED_CONTRACT_EVALUATION :
    			this.populateSupplierUploadedContractEvaluationdModel(builder);
    			break;

    		case SUPPLIER_UPLOADED_CONTRACT_ACCEPTED :
    			this.populateSupplierUploadedContractAcceptedModel(builder);
    			break;

    		case SUPPLIER_UPLOADED_CONTRACT_REJECTED :
    			this.populateSupplierUploadedContractRejectedModel(builder);
    			break;

            case SUPPLIER_CONTRACT_TO_BE_FILLED_OUT :
            	this.populateOwnContractToBeFilledOutModel(builder);
            	break;

            case CONSUMER_DIGITAL_DELIVERY_BY_SUPPLIER :
            	this.populateConsumerDigitalDeliveryBySupplierModel(builder);
            	break;

            case CONSUMER_DIGITAL_DELIVERY_BY_PLATFORM :
            	this.populateConsumerDigitalDeliveryByPlatformModel(builder);
            	break;

            case CONSUMER_PHYSICAL_DELIVERY_BY_SUPPLIER :
            	this.populateConsumerPhysicalDeliveryBySupplierModel(builder);
            	break;

            case CONSUMER_RECEIPT_ISSUED :
            	this.populateConsumerReceiptIssuedModel(builder);
            	break;

            case CONSUMER_INVOICE :
            	this.populateConsumerInvoiceModel(builder);
            	break;

            case CONSUMER_PURCHASE_NOTIFICATION :
            	this.populateConsumerPurchaseNotificationModel(builder);
            	break;

            case CONSUMER_PURCHASE_APPROVED :
            	this.populateConsumerPurchaseApprovedModel(builder);
            	break;

            case CONSUMER_PURCHASE_REJECTION :
            	this.populateConsumerPurchaseRejectionModel(builder);
            	break;

            case CONSUMER_FILLED_OUT_CONTRACT :
            	this.populateOwnContractFilledOutModel(builder);
            	break;

            case CONSUMER_SUPPLIER_CONTRACT_SIGNED :
            	this.populateOwnContractSignedModel(builder);
            	break;

            case MASTER_TEMPLATE_CONTRACT_UPDATE :
            	this.populateMasterTemplateContractUpdateModel(builder);
            	break;

            case FILES_UPLOAD_COMPLETED :
            	this.populateFilesUploadCompletedModel(builder);
            	break;

            case CATALOGUE_HARVEST_COMPLETED :
            	this.populateCatalogueHarvestCompletedModel(builder);
            	break;

        }

        return builder.build();
    }

    private void populateAccountActivationTokenModel(MailModelBuilder builder) {
        final UUID                    userKey = UUID.fromString((String) builder.get("userKey"));
        final EnumActivationTokenType type    = EnumActivationTokenType.fromString((String) builder.get("tokenType"));

        final ActivationTokenEntity tokenEntity = this.activationTokenRepository.findActiveByTypeAndAccountKey(type, userKey).get();

        builder
            .setRecipientName(tokenEntity.getAccount().getFullName())
            .setRecipientAddress(tokenEntity.getEmail())
            .add("name", tokenEntity.getAccount().getFullName())
            .add("token", tokenEntity.getToken())
            .add("url", this.baseUrl);
    }

    private void populateAccountActivationSuccessModel(MailModelBuilder builder) {
        final UUID userKey = UUID.fromString((String) builder.get("userKey"));

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("url", this.baseUrl);
    }

    private void populateAccountActivationWelcomeModel(MailModelBuilder builder) {
        final UUID userKey = UUID.fromString((String) builder.get("userKey"));

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("url", this.baseUrl);
    }

    private void populateAccountPasswordResetModel(MailModelBuilder builder) {
        final UUID userKey = UUID.fromString((String) builder.get("userKey"));

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("url", this.baseUrl);
    }

    private void populateOrderConfirmationModel(MailModelBuilder builder) {
        final UUID userKey  = UUID.fromString((String) builder.get("userKey"));
        final UUID orderKey = UUID.fromString((String) builder.get("orderKey"));

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        final AddressEmbeddable consumerAddress = ((CustomerIndividualEntity) account.getConsumer()).getAddress();

        final OrderEntity     orderEntity     = orderRepository.findByKey(orderKey).get();
        final OrderItemEntity orderItemEntity = orderEntity.getItems().get(0);

        // TODO: Add shipping address to order
        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("orderId", orderEntity.getReferenceNumber())
            .add("orderDate", orderEntity.getCreatedOn())
            .add("orderTotal", orderEntity.getTotalPrice())
            .add("shippingRoad", consumerAddress.getLine1() + " " + consumerAddress.getLine2())
            .add("shippingPostalCode", consumerAddress.getPostalCode())
            .add("shippingCity", consumerAddress.getCity())
            .add("shippingCountry", consumerAddress.getCountry())
            .add("itemName", orderItemEntity.getAssetTitle())
            .add("itemType", orderItemEntity.getType())
            .add("itemVersion", orderItemEntity.getAssetVersion())
            .add("itemVendor", orderItemEntity.getProvider().getProvider().getName())
            .add("itemPrice", orderItemEntity.getTotalPrice());
    }

	private void populateSupplierEvaluationModel(MailModelBuilder builder) {
        final UUID userKey = UUID.fromString((String) builder.get("userKey"));

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName());
	}

	private void populateSupplierDeliveryRequestModel(MailModelBuilder builder) {
        final UUID userKey  = UUID.fromString((String) builder.get("userKey"));
        final UUID orderKey = UUID.fromString((String) builder.get("orderKey"));

        final AccountEntity 	account 		= this.accountRepository.findOneByKey(userKey).get();
        final OrderEntity     	orderEntity     = orderRepository.findByKey(orderKey).get();
        final OrderItemEntity	orderItemEntity = orderEntity.getItems().get(0);
        final AccountEntity		customerEntity	= orderEntity.getConsumer();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("orderId", orderEntity.getReferenceNumber())
            .add("consumerUsername", customerEntity.getUserName())
            .add("productName", orderItemEntity.getAssetTitle())
            ;

	}

	private void populateSupplierDigitalDeliveryModel(MailModelBuilder builder) {
        final UUID userKey  = UUID.fromString((String) builder.get("userKey"));
        final UUID orderKey = UUID.fromString((String) builder.get("orderKey"));

        final AccountEntity 	account 		= this.accountRepository.findOneByKey(userKey).get();
        final OrderEntity     	orderEntity     = orderRepository.findByKey(orderKey).get();
        final OrderItemEntity	orderItemEntity = orderEntity.getItems().get(0);
        final AccountEntity		customerEntity	= orderEntity.getConsumer();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("orderId", orderEntity.getReferenceNumber())
            .add("consumerUsername", customerEntity.getUserName())
            .add("productName", orderItemEntity.getAssetTitle())
            ;
	}

	private void populateSupplierPurchaseReminderModel(MailModelBuilder builder) {
        final UUID userKey  = UUID.fromString((String) builder.get("userKey"));
        final UUID orderKey = UUID.fromString((String) builder.get("orderKey"));

        final AccountEntity 	account 		= this.accountRepository.findOneByKey(userKey).get();
        final OrderEntity     	orderEntity     = orderRepository.findByKey(orderKey).get();
        final OrderItemEntity	orderItemEntity = orderEntity.getItems().get(0);
        final AccountEntity		customerEntity	= orderEntity.getConsumer();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("consumerUsername", customerEntity.getUserName())
            .add("productName", orderItemEntity.getAssetTitle());
	}

	private void populateSupplierAssetAcceptedByHelpdeskModel(MailModelBuilder builder) {
		final UUID userKey  = UUID.fromString((String) builder.get("startUserKey"));
        final UUID draftKey = UUID.fromString((String) builder.get("draftKey"));

        final AccountEntity 	account 		= this.accountRepository.findOneByKey(userKey).get();
        final String 			assetName 		= this.providerAssetService.findOneDraft(draftKey).getTitle();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("assetName", assetName);
	}

	private void populateSupplierAssetRejectedByHelpdeskModel(MailModelBuilder builder) {
		final UUID userKey  = UUID.fromString((String) builder.get("startUserKey"));
        final UUID draftKey = UUID.fromString((String) builder.get("draftKey"));

        final AccountEntity 	account 		= this.accountRepository.findOneByKey(userKey).get();
        final String 			assetName 		= this.providerAssetService.findOneDraft(draftKey).getTitle();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("assetName", assetName);
	}

	private void populateSupplierAssetPublishedModel(MailModelBuilder builder) {
		final UUID userKey  = UUID.fromString((String) builder.get("startUserKey"));
        final UUID draftKey = UUID.fromString((String) builder.get("draftKey"));

        final AccountEntity account 		= this.accountRepository.findOneByKey(userKey).get();

		final String 	assetPublishedId	= this.providerAssetService.findOneDraft(draftKey).getAssetPublished();
		final String 	assetName			= this.catalogueService.findOneFeature(assetPublishedId).getProperties().getTitle();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("assetName", assetName)
            .add("assetURL", this.baseUrl + "/" + "catalogue" + "/" + assetPublishedId);
	}

	private void populateSupplierUploadedContractEvaluationdModel(MailModelBuilder builder) {
        final UUID userKey = UUID.fromString((String) builder.get("userKey"));

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName());

	}

	private void populateSupplierUploadedContractAcceptedModel(MailModelBuilder builder) {
        final UUID userKey 			= UUID.fromString((String) builder.get("userKey"));
        // TODO To be included in input variables
//        final String contractName	= (String) builder.get("contractName");
        final String contractName = "Provider-contract.pdf";

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("contractName", contractName);

	}

	private void populateSupplierUploadedContractRejectedModel(MailModelBuilder builder) {
        final UUID userKey = UUID.fromString((String) builder.get("userKey"));
        // TODO To be included in input variables
//        final String contractName	= (String) builder.get("contractName");
        final String contractName = "Provider-contract.pdf";

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("contractName", contractName);

	}

    private void populateOwnContractToBeFilledOutModel(MailModelBuilder builder) {
        final UUID userKey  = UUID.fromString((String) builder.get("userKey"));
        final UUID orderKey = UUID.fromString((String) builder.get("orderKey"));

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        final OrderEntity     orderEntity     	= orderRepository.findByKey(orderKey).get();
        final OrderItemEntity orderItemEntity 	= orderEntity.getItems().get(0);

        final AccountEntity			customer		= orderEntity.getConsumer();
        final EnumMangopayUserType	customerType	= customer.getConsumer().getType();

        switch (customerType) {
        	case INDIVIDUAL :
                builder
                .setRecipientName(account.getFullName())
                .setRecipientAddress(account.getEmail())
                .add("name", account.getFullName())
                .add("consumerUsername", ((CustomerIndividualEntity) customer.getConsumer()).getAccount().getUserName())
                .add("itemName", orderItemEntity.getAssetTitle())
                .add("consumerFullName", ((CustomerIndividualEntity) customer.getConsumer()).getFullName())
                .add("consumerAddress", ((CustomerIndividualEntity) customer.getConsumer()).getAddress().getLine1() + " " + ((CustomerIndividualEntity) customer.getConsumer()).getAddress().getLine2() + ", " + ((CustomerIndividualEntity) customer.getConsumer()).getAddress().getCity() + " " + ((CustomerIndividualEntity) customer.getConsumer()).getAddress().getCountry())
                .add("contactEmail", ((CustomerIndividualEntity) customer.getConsumer()).getEmail())
                .add("contactPerson", ((CustomerIndividualEntity) customer.getConsumer()).getFullName())
                .add("contactRegNumber","N/A")
                .add("vatNumber", "N/A");
        		break;
        	case PROFESSIONAL :
        		final CustomerProfessionalEntity p =  (CustomerProfessionalEntity)customer.getConsumer();
        		builder
                .setRecipientName(account.getFullName())
                .setRecipientAddress(account.getEmail())
                .add("name", account.getFullName())
                .add("consumerUsername", ((CustomerIndividualEntity) customer.getConsumer()).getAccount().getUserName())
                .add("itemName", orderItemEntity.getAssetTitle())
                .add("consumerFullName", ((CustomerIndividualEntity) customer.getConsumer()).getFullName())
                .add("consumerAddress", ((CustomerIndividualEntity) customer.getConsumer()).getAddress().getLine1() + " " + ((CustomerIndividualEntity) customer.getConsumer()).getAddress().getLine2() + ", " + ((CustomerIndividualEntity) customer.getConsumer()).getAddress().getCity() + " " + ((CustomerIndividualEntity) customer.getConsumer()).getAddress().getCountry())
                .add("contactEmail", ((CustomerIndividualEntity) customer.getConsumer()).getEmail())
                .add("contactPerson", (String.format("%s %s", p.getRepresentative().getFirstName(), p.getRepresentative().getLastName()) .trim()))
                .add("contactRegNumber",p.getCompanyNumber())
                .add("vatNumber", "N/A");
        		break;
		default:
			break;
        }
    }

    private void populateOwnContractFilledOutModel(MailModelBuilder builder) {
        final UUID userKey  = UUID.fromString((String) builder.get("userKey"));
        final UUID orderKey = UUID.fromString((String) builder.get("orderKey"));

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        final OrderEntity     orderEntity     	= orderRepository.findByKey(orderKey).get();
        final OrderItemEntity orderItemEntity 	= orderEntity.getItems().get(0);

        builder
        .setRecipientName(account.getFullName())
        .setRecipientAddress(account.getEmail())
        .add("name", account.getFullName())
        .add("orderId", orderEntity.getReferenceNumber())
        .add("supplierName", orderItemEntity.getProvider().getProvider().getName())
        .add("assetName", orderItemEntity.getAssetTitle());
    }

    private void populateOwnContractSignedModel(MailModelBuilder builder) {
        final UUID userKey  = UUID.fromString((String) builder.get("userKey"));
        final UUID orderKey = UUID.fromString((String) builder.get("orderKey"));

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        final OrderEntity     orderEntity     	= orderRepository.findByKey(orderKey).get();

        builder
        .setRecipientName(account.getFullName())
        .setRecipientAddress(account.getEmail())
        .add("name", account.getFullName())
        .add("orderId", orderEntity.getReferenceNumber());
    }

	private void populateConsumerDigitalDeliveryBySupplierModel(MailModelBuilder builder) {
        final UUID userKey  = UUID.fromString((String) builder.get("userKey"));
        final UUID orderKey = UUID.fromString((String) builder.get("orderKey"));

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        final OrderEntity     orderEntity     = orderRepository.findByKey(orderKey).get();
        final OrderItemEntity orderItemEntity = orderEntity.getItems().get(0);

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("orderId", orderEntity.getReferenceNumber())
            .add("supplierName",orderItemEntity.getProvider().getProvider().getName());

	}

	private void populateConsumerDigitalDeliveryByPlatformModel(MailModelBuilder builder) {
        final UUID userKey  = UUID.fromString((String) builder.get("userKey"));
        final UUID orderKey = UUID.fromString((String) builder.get("orderKey"));

        final AccountEntity account 	= this.accountRepository.findOneByKey(userKey).get();
        final OrderEntity 	orderEntity	= orderRepository.findByKey(orderKey).get();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("orderId", orderEntity.getReferenceNumber());

	}

	private void populateConsumerPhysicalDeliveryBySupplierModel(MailModelBuilder builder) {
        final UUID userKey  = UUID.fromString((String) builder.get("userKey"));
        final UUID orderKey = UUID.fromString((String) builder.get("orderKey"));

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        final OrderEntity     orderEntity     = orderRepository.findByKey(orderKey).get();
        final OrderItemEntity orderItemEntity = orderEntity.getItems().get(0);

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("orderId", orderEntity.getReferenceNumber())
            .add("supplierName", orderItemEntity.getProvider().getProvider().getName());

	}

	private void populateConsumerReceiptIssuedModel(MailModelBuilder builder) {
        final UUID userKey  = UUID.fromString((String) builder.get("userKey"));
        final UUID orderKey = UUID.fromString((String) builder.get("orderKey"));

        final AccountEntity account 	= this.accountRepository.findOneByKey(userKey).get();
        final OrderEntity 	orderEntity	= orderRepository.findByKey(orderKey).get();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("orderId", orderEntity.getReferenceNumber());

	}

	private void populateConsumerInvoiceModel(MailModelBuilder builder) {
        final UUID userKey  = UUID.fromString((String) builder.get("userKey"));
        final UUID orderKey = UUID.fromString((String) builder.get("orderKey"));

        final AccountEntity account 	= this.accountRepository.findOneByKey(userKey).get();
        final OrderEntity 	orderEntity	= orderRepository.findByKey(orderKey).get();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("orderId", orderEntity.getReferenceNumber());

	}

	private void populateConsumerPurchaseNotificationModel(MailModelBuilder builder) {
        final UUID userKey  = UUID.fromString((String) builder.get("userKey"));
        final UUID orderKey = UUID.fromString((String) builder.get("orderKey"));

        final AccountEntity account 	= this.accountRepository.findOneByKey(userKey).get();

        final OrderEntity     orderEntity     	= orderRepository.findByKey(orderKey).get();
        final OrderItemEntity orderItemEntity 	= orderEntity.getItems().get(0);

        builder
        .setRecipientName(account.getFullName())
        .setRecipientAddress(account.getEmail())
        .add("name", account.getFullName())
        .add("productName", orderItemEntity.getAssetTitle());

	}

	private void populateConsumerPurchaseApprovedModel(MailModelBuilder builder) {
        final UUID userKey  = UUID.fromString((String) builder.get("userKey"));
        final UUID orderKey = UUID.fromString((String) builder.get("orderKey"));

        final AccountEntity 	account 		= this.accountRepository.findOneByKey(userKey).get();
        final OrderEntity     	orderEntity     = orderRepository.findByKey(orderKey).get();
        final OrderItemEntity	orderItemEntity = orderEntity.getItems().get(0);

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("orderId", orderEntity.getReferenceNumber())
            .add("supplierName", orderItemEntity.getProvider().getProvider().getName())
            .add("productName", orderItemEntity.getAssetTitle())
            ;
	}

	private void populateConsumerPurchaseRejectionModel(MailModelBuilder builder) {
        final UUID userKey  = UUID.fromString((String) builder.get("userKey"));
        final UUID orderKey = UUID.fromString((String) builder.get("orderKey"));

        final AccountEntity 	account 		= this.accountRepository.findOneByKey(userKey).get();
        final OrderEntity     	orderEntity     = orderRepository.findByKey(orderKey).get();
        final OrderItemEntity	orderItemEntity = orderEntity.getItems().get(0);

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName())
            .add("orderId", orderEntity.getReferenceNumber())
            .add("supplierName", orderItemEntity.getProvider().getProvider().getName())
            .add("productName", orderItemEntity.getAssetTitle())
            ;
	}

	private void populateMasterTemplateContractUpdateModel(MailModelBuilder builder) {
        final UUID userKey = UUID.fromString((String) builder.get("userKey"));

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName());

	}

	private void populateFilesUploadCompletedModel(MailModelBuilder builder) {
        final UUID userKey = UUID.fromString((String) builder.get("userKey"));

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName());

	}

	private void populateCatalogueHarvestCompletedModel(MailModelBuilder builder) {
        final UUID userKey = UUID.fromString((String) builder.get("userKey"));

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("name", account.getFullName());
	}

}