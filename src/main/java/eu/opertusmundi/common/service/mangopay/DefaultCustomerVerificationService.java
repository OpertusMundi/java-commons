package eu.opertusmundi.common.service.mangopay;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mangopay.core.Pagination;
import com.mangopay.core.ResponseException;
import com.mangopay.core.Sorting;
import com.mangopay.core.enumerations.KycDocumentType;
import com.mangopay.core.enumerations.KycStatus;
import com.mangopay.core.enumerations.SortDirection;
import com.mangopay.entities.KycDocument;
import com.mangopay.entities.Ubo;
import com.mangopay.entities.UboDeclaration;
import com.mangopay.entities.User;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.CustomerEntity;
import eu.opertusmundi.common.domain.CustomerKycLevelEntity;
import eu.opertusmundi.common.domain.CustomerProfessionalEntity;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.account.EnumKycLevel;
import eu.opertusmundi.common.model.kyc.CustomerVerificationException;
import eu.opertusmundi.common.model.kyc.CustomerVerificationMessageCode;
import eu.opertusmundi.common.model.kyc.KycDocumentCommand;
import eu.opertusmundi.common.model.kyc.KycDocumentCommandDto;
import eu.opertusmundi.common.model.kyc.KycDocumentDto;
import eu.opertusmundi.common.model.kyc.KycDocumentPageCommandDto;
import eu.opertusmundi.common.model.kyc.KycQueryCommand;
import eu.opertusmundi.common.model.kyc.UboCommandDto;
import eu.opertusmundi.common.model.kyc.UboDeclarationCommand;
import eu.opertusmundi.common.model.kyc.UboDeclarationDto;
import eu.opertusmundi.common.model.kyc.UboDto;
import eu.opertusmundi.common.model.kyc.UboQueryCommand;
import eu.opertusmundi.common.model.kyc.UpdateKycLevelCommand;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.CustomerRepository;
import eu.opertusmundi.common.repository.KycDocumentPageRepository;
import io.jsonwebtoken.lang.Assert;

@Service
@Transactional
public class DefaultCustomerVerificationService extends BaseMangoPayService implements CustomerVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCustomerVerificationService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private KycDocumentPageRepository kycDocumentPageRepository;

    @Override
    public PageResultDto<KycDocumentDto> findAllKycDocuments(KycQueryCommand command) throws CustomerVerificationException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getCustomerKey(), "Expected a non-null customer key");

        try {
            final int     page           = command.getPageIndex() < 0 ? 0 : command.getPageIndex();
            final int     size           = command.getPageSize() < 1 ? 10 : command.getPageSize();
            final String  mangoPayUserId = this.resolveMangopayUserId(command.getCustomerKey(), command.getCustomerType());

            final Sorting sorting = new Sorting();
            sorting.addField("CreationDate", SortDirection.desc);

            // MANGOPAY page index is 1-based
            final List<KycDocument> kycDocuments = this.api.getUserApi().getKycDocuments(mangoPayUserId, new Pagination(page + 1, size), sorting);

            if (kycDocuments == null) {
                return PageResultDto.of(page, size, Collections.emptyList());
            }

            final List<KycDocumentDto> items = kycDocuments.stream()
                .map(KycDocumentDto::from)
                .collect(Collectors.toList());

            return PageResultDto.of(page, size, items);
        } catch (final Exception ex) {
            throw this.wrapException("List KYC Documents", ex, command);
        }
    }

    @Override
    public KycDocumentDto findOneKycDocument(KycDocumentCommand command) throws CustomerVerificationException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getCustomerKey(), "Expected a non-null customer key");
        Assert.notNull(command.getCustomerType(), "Expected a non-null customer type");
        Assert.hasText(command.getKycDocumentId(), "Expected a non-blank KYC declaration id");

        try {
            final String      mangoPayUserId = this.resolveMangopayUserId(command.getCustomerKey(), command.getCustomerType());
            final KycDocument kycDocument    = this.api.getUserApi().getKycDocument(mangoPayUserId, command.getKycDocumentId());

            return KycDocumentDto.from(kycDocument);
        } catch (final Exception ex) {
            throw this.wrapException("Find KYC Document", ex, command);
        }
    }

    @Override
    public KycDocumentDto createKycDocument(KycDocumentCommandDto command) throws CustomerVerificationException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getUserKey(), "Expected a non-null user key");

        try {
            final String      mangoPayUserId = this.resolveMangopayUserId(command.getUserKey(), command.getCustomerType());
            final KycDocument kycDocument    = this.api.getUserApi().createKycDocument(
                mangoPayUserId, KycDocumentType.valueOf(command.getType().toString()), command.getTag()
            );

            return KycDocumentDto.from(kycDocument);
        } catch (final Exception ex) {
            throw this.wrapException("Create KYC Document", ex, command);
        }
    }

    @Override
    public void addPage(KycDocumentPageCommandDto command, byte[] data) throws CustomerVerificationException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getUserKey(), "Expected a non-null user key");
        Assert.hasText(command.getKycDocumentId(), "Expected a non-blank KYC document id");
        Assert.notNull(data, "Expected a non-null data array");

        try {
            final String mangoPayUserId = this.resolveMangopayUserId(command.getUserKey(), command.getCustomerType());

            // Store page metadata
            kycDocumentPageRepository.create(command);
            // Submit page
            this.api.getUserApi().createKycPage(mangoPayUserId, command.getKycDocumentId(), data);
        } catch (final Exception ex) {
            throw this.wrapException("Create KYC Document Page", ex, command);
        }
    }

    @Override
    public KycDocumentDto submitKycDocument(KycDocumentCommand command) throws CustomerVerificationException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getCustomerKey(), "Expected a non-null customer key");
        Assert.notNull(command.getCustomerType(), "Expected a non-null customer type");
        Assert.hasText(command.getKycDocumentId(), "Expected a non-blank KYC document id");

        try {
            final String mangoPayUserId = this.resolveMangopayUserId(command.getCustomerKey(), command.getCustomerType());
            KycDocument  kycDocument    = this.api.getUserApi().getKycDocument(mangoPayUserId, command.getKycDocumentId());

            kycDocument.setStatus(KycStatus.VALIDATION_ASKED);

            kycDocument = this.api.getUserApi().updateKycDocument(mangoPayUserId, kycDocument);

            return KycDocumentDto.from(kycDocument);
        } catch (final Exception ex) {
            throw this.wrapException("Submit UBO Declaration", ex, command);
        }
    }

    @Override
    public PageResultDto<UboDeclarationDto> findAllUboDeclarations(UboQueryCommand command) throws CustomerVerificationException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getCustomerKey(), "Expected a non-null customer key");
        Assert.notNull(command.getCustomerType(), "Expected a non-null customer type");

        try {
            final int     page           = command.getPageIndex() < 0 ? 0 : command.getPageIndex();
            final int     size           = command.getPageSize() < 1 ? 10 : command.getPageSize();
            final String  mangoPayUserId = this.resolveMangopayUserId(command.getCustomerKey(), command.getCustomerType());

            final Sorting sorting = new Sorting();
            sorting.addField("CreationDate", SortDirection.desc);

            // MANGOPAY page index is 1-based
            final List<UboDeclaration> uboDeclarations = this.api.getUboDeclarationApi().getAll(mangoPayUserId, new Pagination(page + 1, size), sorting);

            if (uboDeclarations == null) {
                return PageResultDto.of(page, size, Collections.emptyList());
            }

            final List<UboDeclarationDto> items = uboDeclarations.stream()
                .map(u -> UboDeclarationDto.from(u, false))
                .collect(Collectors.toList());

            return PageResultDto.of(page, size, items);
        } catch (final Exception ex) {
            throw this.wrapException("List UBO Declarations", ex, command);
        }
    }

    @Override
    public UboDeclarationDto findOneUboDeclaration(UboDeclarationCommand command) throws CustomerVerificationException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getCustomerKey(), "Expected a non-null provider key");
        Assert.notNull(command.getCustomerType(), "Expected a non-null customer type");
        Assert.hasText(command.getUboDeclarationId(), "Expected a non-blank UBO declaration id");

        try {
            final String         mangoPayUserId = this.resolveMangopayUserId(command.getCustomerKey(), command.getCustomerType());
            final UboDeclaration uboDeclaration = this.api.getUboDeclarationApi().get(mangoPayUserId, command.getUboDeclarationId());

            return UboDeclarationDto.from(uboDeclaration, true);
        } catch (final Exception ex) {
            throw this.wrapException("Find UBO Declaration", ex, command);
        }
    }

    @Override
    public UboDeclarationDto createUboDeclaration(UboDeclarationCommand command) throws CustomerVerificationException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getCustomerKey(), "Expected a non-null provider key");
        Assert.notNull(command.getCustomerType(), "Expected a non-null customer type");

        try {
            final String         mangoPayUserId = this.resolveMangopayUserId(command.getCustomerKey(), command.getCustomerType());
            final UboDeclaration uboDeclaration = this.api.getUboDeclarationApi().create(mangoPayUserId);

            return UboDeclarationDto.from(uboDeclaration, true);
        } catch (final Exception ex) {
            throw this.wrapException("Create UBO Declaration", ex, command);
        }
    }

    @Override
    public UboDto addUbo(UboCommandDto command) throws CustomerVerificationException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getCustomerKey(), "Expected a non-null provider key");
        Assert.notNull(command.getCustomerType(), "Expected a non-null customer type");
        Assert.hasText(command.getUboDeclarationId(), "Expected a non-blank UBO declaration id");

        try {
            final String mangoPayUserId = this.resolveMangopayUserId(command.getCustomerKey(), command.getCustomerType());
            final Ubo    ubo            = this.api.getUboDeclarationApi().createUbo(
                mangoPayUserId, command.getUboDeclarationId(), command.toMangoPayUbo()
            );

            return UboDto.from(ubo);
        } catch (final Exception ex) {
            throw this.wrapException("Add UBO", ex, command);
        }
    }

    @Override
    public UboDto updateUbo(UboCommandDto command) throws CustomerVerificationException {
        return this.updateUboImpl(command);
    }

    @Override
    public UboDto removeUbo(UboCommandDto command) throws CustomerVerificationException {
        // Mark UBO as inactive to remove
        command.setActive(false);

        return this.updateUboImpl(command);
    }

    private UboDto updateUboImpl(UboCommandDto command) throws CustomerVerificationException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getCustomerKey(), "Expected a non-null provider key");
        Assert.notNull(command.getCustomerType(), "Expected a non-null customer type");
        Assert.hasText(command.getUboDeclarationId(), "Expected a non-blank UBO declaration id");
        Assert.hasText(command.getUboId(), "Expected a non-blank ubo id");

        try {
            final String mangoPayUserId = this.resolveMangopayUserId(command.getCustomerKey(), command.getCustomerType());
            final Ubo    ubo            = this.api.getUboDeclarationApi().updateUbo(
                mangoPayUserId, command.getUboDeclarationId(), command.toMangoPayUbo()
            );

            return UboDto.from(ubo);
        } catch (final Exception ex) {
            throw this.wrapException("Remove UBO", ex, command);
        }
    }

    @Override
    public UboDeclarationDto submitUboDeclaration(UboDeclarationCommand command) throws CustomerVerificationException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getCustomerKey(), "Expected a non-null provider key");
        Assert.notNull(command.getCustomerType(), "Expected a non-null customer type");
        Assert.hasText(command.getUboDeclarationId(), "Expected a non-blank UBO declaration id");

        try {
            final String         mangoPayUserId = this.resolveMangopayUserId(command.getCustomerKey(), command.getCustomerType());
            final UboDeclaration uboDeclaration = this.api.getUboDeclarationApi().submitForValidation(
                mangoPayUserId, command.getUboDeclarationId()
            );

            return UboDeclarationDto.from(uboDeclaration, true);
        } catch (final Exception ex) {
            throw this.wrapException("Submit UBO Declaration", ex, command);
        }
    }

    @Override
    public AccountDto refreshCustomerKycLevel(UUID accountKey) throws CustomerVerificationException {
        AccountEntity                    account  = this.getAccount(accountKey);
        final CustomerEntity             consumer = account.getProfile().getConsumer();
        final CustomerProfessionalEntity provider = account.getProfile().getProvider();

        if (consumer != null) {
            UpdateKycLevelCommand consumerCommand = UpdateKycLevelCommand.of(consumer.getPaymentProviderUser(), ZonedDateTime.now());
            this.updateCustomerKycLevel(consumerCommand);
        }
        if (provider != null) {
            UpdateKycLevelCommand providerCommand = UpdateKycLevelCommand.of(provider.getPaymentProviderUser(), ZonedDateTime.now());
            this.updateCustomerKycLevel(providerCommand);
        }

        account = this.getAccount(accountKey);

        return account.toDto(true);
    }

    @Override
    public CustomerDto updateCustomerKycLevel(UpdateKycLevelCommand command) throws CustomerVerificationException {
        try {
            // Check customer
            final CustomerEntity customerEntity = this.customerRepository
                .findCustomerByProviderUserId(command.getProviderUserId())
                .orElse(null);

            if(customerEntity == null) {
                throw new CustomerVerificationException(
                    CustomerVerificationMessageCode.PLATFORM_CUSTOMER_NOT_FOUND,
                    String.format("[OpertusMundi] Customer for provider user [%s] was not found", command.getProviderUserId())
                );
            }

            // Fetch payment provider user
            final User userObject = this.api.getUserApi().get(command.getProviderUserId());

            if(userObject == null) {
                throw new CustomerVerificationException(
                    CustomerVerificationMessageCode.PROVIDER_USER_NOT_FOUND,
                    String.format("[OpertusMundi] Provider user [%s] was not found", command.getProviderUserId())
                );
            }

            final EnumKycLevel kycLevel = EnumKycLevel.from(userObject.getKycLevel());

            // Ignore redundant updates
            if (customerEntity.getKycLevel() == kycLevel) {
                return customerEntity.toDto();
            }

            customerEntity.setKycLevel(kycLevel);

            final CustomerKycLevelEntity level = new CustomerKycLevelEntity();
            level.setCustomer(customerEntity);
            level.setLevel(customerEntity.getKycLevel());
            level.setUpdatedOn(command.getUpdatedOn());
            customerEntity.getLevelHistory().add(level);

            this.customerRepository.saveAndFlush(customerEntity);

            return customerEntity.toDto();
        } catch (final Exception ex) {
            throw this.wrapException("Update KYC Level", ex, command);
        }
    }

    private AccountEntity getAccount(UUID key) throws CustomerVerificationException {
        final AccountEntity account = this.accountRepository.findOneByKey(key).orElse(null);

        if (account == null) {
            throw new CustomerVerificationException(
                CustomerVerificationMessageCode.ACCOUNT_NOT_FOUND,
                String.format("[Customer Verification] OpertusMundi user [%s] was not found", key)
            );
        }

        return account;
    }

    private void ensureCustomer(CustomerEntity consumer, UUID key, EnumCustomerType userType) throws CustomerVerificationException {
        if (consumer == null) {
            throw new CustomerVerificationException(
                CustomerVerificationMessageCode.PLATFORM_CUSTOMER_NOT_FOUND,
                String.format("[Customer Verification] %s registration was not found for account with key [%s]", userType, key)
            );
        }
    }

    private String resolveMangopayUserId(UUID customerKey, EnumCustomerType userType) throws CustomerVerificationException {
        final AccountEntity account = this.getAccount(customerKey);
        CustomerEntity      customer;

        switch (userType) {
            case CONSUMER :
                customer = account.getProfile().getConsumer();
                break;
            case PROVIDER :
                customer = account.getProfile().getProvider();
                break;
            default :
                throw new CustomerVerificationException(
                    CustomerVerificationMessageCode.CUSTOMER_TYPE_NOT_SUPPORTED,
                    String.format("[Customer Verification] Customer type %s for customer %s is not supported", userType, customerKey)
                );
        }

        this.ensureCustomer(customer, customerKey, userType);

        return customer.getPaymentProviderUser();
    }

    /**
     * Wraps an exception with {@link CustomerVerificationException}
     *
     * @param operation
     * @param ex
     * @return
     */
    private CustomerVerificationException wrapException(String operation, Exception ex, Object command) {
        final String commandText = command == null ? "-" : command.toString();

        // Ignore service exceptions
        if (ex instanceof CustomerVerificationException) {
            return (CustomerVerificationException) ex;
        }

        // MANGOPAY exceptions
        if (ex instanceof ResponseException) {
            final String message = String.format(
                "MANGOPAY operation has failed. [operation=%s, apiMessage=%s, command=[%s]]",
                operation, ((ResponseException) ex).getApiMessage(), commandText
            );

            logger.error(message, ex);

            return new CustomerVerificationException(CustomerVerificationMessageCode.API_ERROR, message, ex);
        }

        // Global exception handler
        final String message = String.format(
                "MANGOPAY operation has failed. [operation=%s, command=[%s]]",
                operation, commandText
            );

        logger.error(message, ex);

        return new CustomerVerificationException(CustomerVerificationMessageCode.UNKNOWN, message, ex);
    }

}
