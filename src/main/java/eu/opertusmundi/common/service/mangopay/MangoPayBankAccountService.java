package eu.opertusmundi.common.service.mangopay;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.mangopay.core.Address;
import com.mangopay.core.Pagination;
import com.mangopay.core.ResponseException;
import com.mangopay.core.enumerations.BankAccountType;
import com.mangopay.core.interfaces.BankAccountDetails;
import com.mangopay.entities.BankAccount;
import com.mangopay.entities.subentities.BankAccountDetailsIBAN;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.AddressEmbeddable;
import eu.opertusmundi.common.domain.CustomerBankAccountEmbeddable;
import eu.opertusmundi.common.domain.CustomerDraftEntity;
import eu.opertusmundi.common.domain.CustomerDraftProfessionalEntity;
import eu.opertusmundi.common.domain.CustomerProfessionalEntity;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.BankAccountDto;
import eu.opertusmundi.common.model.account.EnumCustomerRegistrationStatus;
import eu.opertusmundi.common.model.account.EnumMangopayUserType;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.UserPaginationCommand;
import eu.opertusmundi.common.model.payment.UserRegistrationCommand;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.util.MangopayUtils;

@Service
@Transactional
public class MangoPayBankAccountService extends BaseMangoPayService implements BankAccountService {

    private static final Logger logger = LoggerFactory.getLogger(MangoPayBankAccountService.class);
    
    @Autowired
    public MangoPayBankAccountService(AccountRepository accountRepository) {
        super(accountRepository);
    }
    
    @Override
    public AccountDto createBankAccount(UserRegistrationCommand command) {
        try {
            BankAccount bankAccount;

            // Get account
            final AccountEntity account = this.getAccount(command.getUserKey());

            // Resolve registration
            final CustomerDraftEntity registration   = this.resolveCustomerRegistration(account, command.getType(), command.getRegistrationKey());
            final String              idempotencyKey = registration.getBankAccountIdempotentKey().toString();

            // OpertusMundi user must be registered to the MangoPay platform
            if (StringUtils.isBlank(registration.getPaymentProviderUser())) {
                throw new PaymentException(String.format("[MANGOPAY] OpertusMundi User [%s] is not registered", command.getUserKey()));
            }

            // Registration must be of type PROFESSIONAL
            if (registration.getType() != EnumMangopayUserType.PROFESSIONAL) {
                throw new PaymentException(
                    String.format("[MANGOPAY] Cannot create bank account for user [%s] of type [%s]", account.getEmail(), registration.getType())
                );
            }

            final CustomerDraftProfessionalEntity profRegistration = (CustomerDraftProfessionalEntity) registration;

            // Check if this is a retry
            bankAccount = this.<BankAccount>getResponse(idempotencyKey);

            if (bankAccount != null) {
                // Bank account has already been created. Check MangoPay bank account Id
                if (!StringUtils.isBlank(profRegistration.getBankAccount().getId()) &&
                    !bankAccount.getId().equals(profRegistration.getBankAccount().getId())
                ) {
                    throw new PaymentException(String.format(
                        "[MANGOPAY] Multiple keys [%s, %s] have been found for the bank account of OpertusMundi user [%s](%s)",
                        profRegistration.getBankAccount().getId(), bankAccount.getId(), account.getEmail(), account.getKey()
                    ));
                }
            } else {
                // Create bank account
                bankAccount = this.createBankAccount(profRegistration.getBankAccount());

                bankAccount.setTag(command.getUserKey().toString());
                bankAccount.setUserId(registration.getPaymentProviderUser());

                bankAccount = this.api.getUserApi().createBankAccount(
                    idempotencyKey, registration.getPaymentProviderUser(), bankAccount
                );
            }

            // Update registration
            profRegistration.getBankAccount().setId(bankAccount.getId());
            profRegistration.getBankAccount().setTag(bankAccount.getTag());

            this.accountRepository.saveAndFlush(account);

            return account.toDto();
        } catch (final Exception ex) {
            throw this.wrapException("Create Bank Account", ex, command);
        }
    }

    @Override
    public AccountDto updateBankAccount(UserRegistrationCommand command) {
        try {
            final var registrationKey = command.getRegistrationKey();
            final var account         = this.getAccount(command.getUserKey());
            final var customer        = account.getProfile().getProvider();
            final var registration    = (CustomerDraftProfessionalEntity) this.resolveCustomerRegistration(account, command.getType(), registrationKey);

            if(registration == null) {
                throw new PaymentException(String.format(
                    "[MANGOPAY] Provider registration was not found for account with key [%s]",
                    command.getUserKey()
                ));
            }

            if (registration.getStatus() != EnumCustomerRegistrationStatus.SUBMITTED) {
                throw new PaymentException(String.format(
                    "[MANGOPAY] Invalid registration state [%s] for key [%s]. Expected [SUBMITTED]",
                    registration.getStatus(), registrationKey
                ));
            }

            // If no attribute is updated, skip update
            if (customer.getBankAccount().equals(registration.getBankAccount())) {
                return account.toDto();
            }

            final String mangoPayUserId        = customer.getPaymentProviderUser();
            final String mangoPayBankAccountId = customer.getBankAccount().getId();

            // A linked account may already exists
            if (!StringUtils.isBlank(mangoPayBankAccountId)) {
                final BankAccount currentBankAccount = this.api.getUserApi().getBankAccount(mangoPayUserId, mangoPayBankAccountId);

                // Deactivate the current bank account of the provider
                this.deactivateBankAccount(mangoPayUserId, currentBankAccount);
                customer.getBankAccount().setId(null);

                this.accountRepository.saveAndFlush(account);
            }

            // Create new account
            final AccountDto result = this.createBankAccount(command);

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Update Bank Account", ex, command);
        }
    }

    @Override
    public List<BankAccountDto> getBankAccounts(UserPaginationCommand command) throws PaymentException {
        try {
            final AccountEntity              account  = this.getAccount(command.getUserKey());
            final CustomerProfessionalEntity customer = account.getProfile().getProvider();

            this.ensureCustomer(customer, command.getUserKey());

            final int               page           = command.getPage() < 1 ? 1 : command.getPage();
            final int               size           = command.getSize() < 1 ? 10 : command.getSize();
            final String            mangoPayUserId = customer.getPaymentProviderUser();
            final List<BankAccount> bankAccounts   = this.api.getUserApi().getBankAccounts(mangoPayUserId, new Pagination(page, size), null);

            if (bankAccounts == null) {
                return Collections.emptyList();
            }

            return bankAccounts.stream().map(BankAccountDto::from).collect(Collectors.toList());
        } catch (final Exception ex) {
            throw this.wrapException("Get User Bank Accounts", ex, command);
        }
    }

    private void deactivateBankAccount(String userId, BankAccount bankAccount) throws PaymentException {
        Assert.notNull(bankAccount, "Expected a non-null bank account");

        // Check if this is a retry
        if (!bankAccount.isActive()) {
            return;
        }

        try {
            bankAccount.setActive(false);

            this.api.getUserApi().updateBankAccount(userId, bankAccount, bankAccount.getId());
        } catch (final ResponseException ex) {
            logger.error("MANGOPAY operation has failed", ex);

            throw new PaymentException("[MANGOPAY] Error: " + ex.getApiMessage(), ex);
        } catch (final PaymentException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("MANGOPAY bank account update has failed", ex);

            throw new PaymentException("[MANGOPAY] Bank account update has failed", ex);
        }
    }
    
    private BankAccount createBankAccount(CustomerBankAccountEmbeddable a) {
        return this.createBankAccount(a, null);
    }

    private BankAccount createBankAccount(CustomerBankAccountEmbeddable a, String id) {
        final BankAccount bankAccount = new BankAccount();

        bankAccount.setActive(true);
        bankAccount.setDetails(this.createBankAccountDetails(a));
        bankAccount.setId(id);
        bankAccount.setOwnerAddress(this.createAddress(a.getOwnerAddress()));
        bankAccount.setOwnerName(a.getOwnerName());
        bankAccount.setType(BankAccountType.IBAN);

        return bankAccount;
    }

    private BankAccountDetails createBankAccountDetails(CustomerBankAccountEmbeddable a) {
        final BankAccountDetailsIBAN d = new BankAccountDetailsIBAN();

        d.setBic(a.getBic());
        d.setIban(a.getIban());

        return d;
    }

    private Address createAddress(AddressEmbeddable e) {
        final Address a = new Address();

        a.setAddressLine1(e.getLine1());
        a.setAddressLine2(e.getLine2());
        a.setCity(e.getCity());
        a.setCountry(MangopayUtils.countryFromString(e.getCountry()));
        a.setPostalCode(e.getPostalCode());
        a.setRegion(e.getRegion());

        return a;
    }
    
    private PaymentException wrapException(String operation, Exception ex, Object command) {
        return super.wrapException(operation, ex, command, logger);
    }
}