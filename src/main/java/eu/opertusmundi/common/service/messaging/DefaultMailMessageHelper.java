package eu.opertusmundi.common.service.messaging;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.ibm.icu.text.MessageFormat;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.ActivationTokenEntity;
import eu.opertusmundi.common.domain.AddressEmbeddable;
import eu.opertusmundi.common.domain.CustomerIndividualEntity;
import eu.opertusmundi.common.domain.MailTemplateEntity;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.email.EmailAddressDto;
import eu.opertusmundi.common.model.email.EnumMailType;
import eu.opertusmundi.common.model.email.MailMessageCode;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.ActivationTokenRepository;
import eu.opertusmundi.common.repository.MailTemplateRepository;
import eu.opertusmundi.common.repository.OrderRepository;

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

    @Override
    public String composeSubject(EnumMailType type, Map<String, Object> variables) {
        Assert.notNull(type, "Expected a non-null mail type");

        final MailTemplateEntity template = templateRepository.findOneByType(type).orElse(null);
        if (template == null) {
            throw new ServiceException(MailMessageCode.TEMPLATE_NOT_FOUND, String.format("Mail template %s was not found", type));
        }

        switch (type) {
            case ACCOUNT_ACTIVATION_TOKEN :
            case ACCOUNT_ACTIVATION_SUCCESS :
            case ORDER_CONFIRMATION:
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
    public Map<String, Object> createModel(EnumMailType type, MailModelBuilder builder) {
        Assert.notNull(type, "Expected a non-null mail type");

        switch (type) {
            case ACCOUNT_ACTIVATION_TOKEN :
                this.populateAccountActivationTokenModel(builder);
                break;

            case ACCOUNT_ACTIVATION_SUCCESS :
                this.populateAccountActivationSuccessModel(builder);
                break;

            case ORDER_CONFIRMATION:
            	this.populateOrderConfirmationModel(builder);
            	break;

        }

        return builder.build();
    }

    private void populateAccountActivationTokenModel(MailModelBuilder builder) {
        final UUID userKey = UUID.fromString((String) builder.get("userKey"));

        final ActivationTokenEntity tokenEntity = this.activationTokenRepository.findOneActiveByAccountKey(userKey).get();

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

    private void populateOrderConfirmationModel(MailModelBuilder builder) {
        final UUID userKey  = UUID.fromString((String) builder.get("userKey"));
        final UUID orderKey = UUID.fromString((String) builder.get("orderKey"));

        final AccountEntity account = this.accountRepository.findOneByKey(userKey).get();

        final AddressEmbeddable consumerAddress = ((CustomerIndividualEntity) account.getConsumer()).getAddress();

        final OrderEntity     orderEntity     = orderRepository.findOrderEntityByKey(orderKey).get();
        final OrderItemEntity orderItemEntity = orderEntity.getItems().get(0);

        // TODO: Add shipping address to order
        builder
            .setRecipientName(account.getFullName())
            .setRecipientAddress(account.getEmail())
            .add("orderId", orderEntity.getId())
            .add("orderDate", orderEntity.getCreatedOn())
            .add("orderTotal", orderEntity.getTotalPrice())
            .add("shippingRoad", consumerAddress.getLine1() + " " + consumerAddress.getLine2())
            .add("shippingPostalCode", consumerAddress.getPostalCode())
            .add("shippingCity", consumerAddress.getCity())
            .add("shippingCountry", consumerAddress.getCountry())
            .add("itemName", orderItemEntity.getDescription())
            .add("itemType", orderItemEntity.getType())
            .add("itemVersion", orderItemEntity.getAssetVersion())
            .add("itemVendor", orderItemEntity.getProvider())
            .add("itemPrice", orderItemEntity.getTotalPrice());
    }

}
