package eu.opertusmundi.common.service.mangopay;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mangopay.core.Pagination;
import com.mangopay.core.ResponseException;
import com.mangopay.core.enumerations.CurrencyIso;
import com.mangopay.core.enumerations.FundsType;
import com.mangopay.entities.Wallet;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.CustomerDraftEntity;
import eu.opertusmundi.common.domain.CustomerEntity;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.payment.ClientWalletDto;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.UserRegistrationCommand;
import eu.opertusmundi.common.model.payment.WalletDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.util.StreamUtils;

@Service
@Transactional
public class MangoPayWalletService extends BaseMangoPayService implements WalletService {

    private static final Logger logger = LoggerFactory.getLogger(MangoPayWalletService.class);

    @Autowired
    public MangoPayWalletService(AccountRepository accountRepository) {
        super(accountRepository);
    }

    @Override
    public List<ClientWalletDto> getClientWallets() throws PaymentException {
        try {
            final List<ClientWalletDto> result        = new ArrayList<>();
            final List<Wallet>          feeWallets    = this.api.getClientApi().getWallets(FundsType.FEES, new Pagination(0, 20));
            final List<Wallet>          creditWallets = this.api.getClientApi().getWallets(FundsType.CREDIT, new Pagination(0, 20));

            StreamUtils.from(feeWallets).map(ClientWalletDto::from).forEach(result::add);
            StreamUtils.from(creditWallets).map(ClientWalletDto::from).forEach(result::add);

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Get client wallets", ex);
        }
    }


    @Override
    public AccountDto createWallet(UserRegistrationCommand command) {
        try {
            Wallet wallet;

            // Get account
            final AccountEntity account = this.getAccount(command.getUserKey());

            // Resolve registration
            final CustomerDraftEntity registration   = this.resolveCustomerRegistration(account, command.getType(), command.getRegistrationKey());
            final String              idempotencyKey = registration.getWalletIdempotentKey().toString();

            // OpertusMundi user must be registered to the MangoPay platform
            if (StringUtils.isBlank(registration.getPaymentProviderUser())) {
                throw new PaymentException(String.format("[MANGOPAY] OpertusMundi User [%s] is not registered", command.getUserKey()));
            }

            // Check if this is a retry
            wallet = this.<Wallet>getResponse(idempotencyKey);

            if (wallet != null) {
                // Wallet has already been created. Check MangoPay wallet Id
                if (!StringUtils.isBlank(registration.getPaymentProviderWallet()) &&
                    !wallet.getId().equals(registration.getPaymentProviderWallet())
                ) {
                    throw new PaymentException(String.format(
                        "[MANGOPAY] Multiple keys [%s, %s] have been found for the wallet of OpertusMundi user [%s](%s)",
                        registration.getPaymentProviderWallet(), wallet.getId(), account.getEmail(), account.getKey()
                    ));
                }
            } else {
                // Create wallet
                final ArrayList<String> owners = new ArrayList<>();

                owners.add(registration.getPaymentProviderUser());

                wallet = new Wallet();

                wallet.setCurrency(CurrencyIso.EUR);
                wallet.setDescription(this.getWalletDescription(account));
                wallet.setFundsType(FundsType.DEFAULT);
                wallet.setOwners(owners);
                wallet.setTag(command.getUserKey().toString());

                wallet = this.api.getWalletApi().create(idempotencyKey, wallet);
            }

            // Update registration
            registration.setPaymentProviderWallet(wallet.getId());

            this.accountRepository.saveAndFlush(account);

            return account.toDto();
        } catch (final Exception ex) {
            throw this.wrapException("Create Wallet", ex, command);
        }
    }


    @Override
    public AccountDto refreshUserWallets(UUID userKey) throws PaymentException {
        try {
            final AccountEntity  account  = this.accountRepository.findOneByKey(userKey).orElse(null);
            final CustomerEntity consumer = account.getConsumer();
            final CustomerEntity provider = account.getProvider();

            if (consumer != null) {
                this.ensureCustomer(consumer, userKey);

                final String    walletId = consumer.getPaymentProviderWallet();
                final Wallet    wallet   = this.api.getWalletApi().get(walletId);
                final WalletDto result   = WalletDto.from(wallet);

                consumer.setWalletFunds(result.getAmount());
                consumer.setWalletFundsUpdatedOn(ZonedDateTime.now());
            }
            if (provider != null) {
                this.ensureCustomer(provider, userKey);

                final String    walletId = provider.getPaymentProviderWallet();
                final Wallet    wallet   = this.api.getWalletApi().get(walletId);
                final WalletDto result   = WalletDto.from(wallet);

                provider.setWalletFunds(result.getAmount());
                provider.setWalletFundsUpdatedOn(ZonedDateTime.now());
            }

            this.accountRepository.saveAndFlush(account);

            return account.toDto();
        } catch (final ResponseException ex) {
            logger.error("Failed to load customer wallet", ex);

            throw new PaymentException("[MANGOPAY] Error: " + ex.getApiMessage(), ex);
        } catch (final PaymentException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw this.wrapException("Get Wallet", ex, userKey);
        }
    }

    @Override
    public WalletDto updateCustomerWalletFunds(UUID userKey, EnumCustomerType type) throws PaymentException {
        try {
            final AccountEntity  account  = this.accountRepository.findOneByKey(userKey).orElse(null);
            final CustomerEntity customer = type == EnumCustomerType.CONSUMER ? account.getConsumer() : account.getProvider();

            this.ensureCustomer(customer, userKey);

            final String    walletId = customer.getPaymentProviderWallet();
            final Wallet    wallet   = this.api.getWalletApi().get(walletId);
            final WalletDto result   = WalletDto.from(wallet);

            customer.setWalletFunds(result.getAmount());
            customer.setWalletFundsUpdatedOn(ZonedDateTime.now());

            this.accountRepository.saveAndFlush(account);

            return result;
        } catch (final ResponseException ex) {
            logger.error("Failed to load customer wallet", ex);

            throw new PaymentException("[MANGOPAY] Error: " + ex.getApiMessage(), ex);
        } catch (final PaymentException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw this.wrapException("Get Wallet", ex, userKey);
        }
    }

    private String getWalletDescription(AccountEntity a) {
        return String.format("Default wallet");
    }

    private PaymentException wrapException(String operation, Exception ex) {
        return super.wrapException(operation, ex, null, logger);
    }

    private PaymentException wrapException(String operation, Exception ex, Object command) {
        return super.wrapException(operation, ex, command, logger);
    }

}
