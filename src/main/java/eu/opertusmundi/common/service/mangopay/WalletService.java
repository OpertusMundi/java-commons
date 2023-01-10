package eu.opertusmundi.common.service.mangopay;

import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.payment.ClientWalletDto;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.UserRegistrationCommand;
import eu.opertusmundi.common.model.payment.WalletDto;

public interface WalletService {

    /**
     * Get client wallets
     *
     * @return
     * @throws PaymentException
     */
    List<ClientWalletDto> getClientWallets() throws PaymentException;

    /**
     * Creates a new waller in the external payment service and links it to a
     * OpertusMundi account
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    AccountDto createWallet(UserRegistrationCommand command) throws PaymentException;

    /**
     * Refreshes all customer wallets from the remote payment provider
     *
     * @param userKey
     * @return
     * @throws PaymentException
     */
    AccountDto refreshUserWallets(UUID userKey) throws PaymentException;

    /**
     * Refreshes a customer wallet from the remote payment provider
     *
     * @param userKey
     * @param type
     * @return
     * @throws PaymentException
     */
    WalletDto refreshUserWallets(UUID userKey, EnumCustomerType type) throws PaymentException;

}
