package eu.opertusmundi.common.service.mangopay;

import java.util.List;

import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.BankAccountDto;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.UserPaginationCommand;
import eu.opertusmundi.common.model.payment.UserRegistrationCommand;

public interface BankAccountService {


    /**
     * Create a new bank account in the external payment service and links it to
     * a OpertusMundi account
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    AccountDto createBankAccount(UserRegistrationCommand command) throws PaymentException;

    /**
     * Update an existing bank account in the external payment service.
     *
     * <p>
     * MANGOPAY does not support updating an account. If any attribute of the account is changed,
     * the account is deactivated and a new one is created.
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    AccountDto updateBankAccount(UserRegistrationCommand command) throws PaymentException;
    

    /**
     * Get user bank accounts.
     *
     * MangoPay pagination is 1-based.
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    List<BankAccountDto> getBankAccounts(UserPaginationCommand command) throws PaymentException;
}
