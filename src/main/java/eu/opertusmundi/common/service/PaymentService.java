package eu.opertusmundi.common.service;

import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.BankAccountDto;
import eu.opertusmundi.common.model.location.Location;
import eu.opertusmundi.common.model.order.CartDto;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.model.payment.BankwirePayInCommand;
import eu.opertusmundi.common.model.payment.CardDirectPayInCommand;
import eu.opertusmundi.common.model.payment.CardDto;
import eu.opertusmundi.common.model.payment.CardRegistrationCommandDto;
import eu.opertusmundi.common.model.payment.CardRegistrationDto;
import eu.opertusmundi.common.model.payment.ClientDto;
import eu.opertusmundi.common.model.payment.EnumPayInSortField;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.PayOutCommandDto;
import eu.opertusmundi.common.model.payment.PayOutDto;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.TransferCommandDto;
import eu.opertusmundi.common.model.payment.TransferDto;
import eu.opertusmundi.common.model.payment.UserCardCommand;
import eu.opertusmundi.common.model.payment.UserCommand;
import eu.opertusmundi.common.model.payment.UserPaginationCommand;
import eu.opertusmundi.common.model.payment.UserRegistrationCommand;

public interface  PaymentService {

    /**
     * Creates a new order from a cart
     *
     * @param cart
     * @return
     * @throws PaymentException
     */
    OrderDto createOrderFromCart(CartDto cart, Location location) throws PaymentException;

    /**
     * Get client registration information
     *
     * @return
     * @throws PaymentException
     */
    ClientDto getClient() throws PaymentException;

    /**
     * Create a new user in the external payment service and link it to a
     * OpertusMundi account
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    AccountDto createUser(UserRegistrationCommand command) throws PaymentException;

    /**
     * Update an existing user in the external payment service. The account must
     * already be lined to a OpertusMundi account
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    AccountDto updateUser(UserRegistrationCommand command) throws PaymentException;

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

    /**
     * Get user registered cards
     *
     * MangoPay pagination is 1-based.
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    List<CardDto> getRegisteredCards(UserPaginationCommand command) throws PaymentException;

    /**
     * Get user registered card
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    CardDto getRegisteredCard(UserCardCommand command) throws PaymentException;

    /**
     * Deactivate card
     *
     * @param command
     * @throws PaymentException
     */
    void deactivateCard(UserCardCommand command) throws PaymentException;

    /**
     * Create card registration
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    CardRegistrationDto createCardRegistration(UserCommand command) throws PaymentException;

    /**
     * Register card to user account
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    String registerCard(CardRegistrationCommandDto command) throws PaymentException;

    /**
     * Get PayIn by key
     *
     * @param userId
     * @param payInKey
     * @return
     */
    PayInDto getPayIn(Integer userId, UUID payInKey);

    /**
     * Query payment provider service for PayIn transaction status
     *
     * @param payIn
     * @return
     * @throws PaymentException
     */
    EnumTransactionStatus getTransactionStatus(String payIn) throws PaymentException;

    /**
     * Search consumer PayIns
     *
     * @param userKey
     * @param status
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    PageResultDto<PayInDto> findAllConsumerPayIns(
        UUID userKey, EnumTransactionStatus status, int pageIndex, int pageSize, EnumPayInSortField orderBy, EnumSortingOrder order
    );

    /**
     * Create bank wire PayIn for a consumer order
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    PayInDto createPayInBankwireForOrder(BankwirePayInCommand command) throws PaymentException;

    /**
     * Crate direct PayIn with a registered card for the specified order
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    PayInDto createPayInCardDirectForOrder(CardDirectPayInCommand command) throws PaymentException;

    /**
     * Update PayIn
     *
     * This method checks the payment provider for updates of a specific PayIn.
     * A webhook may invoke this method to update a pending bankwire or credit
     * card PyaIn.
     *
     * @param payIn The payment provider unique PayIn identifier
     * @return
     * @throws PaymentException
     */
    PayInDto updatePayIn(String payIn) throws PaymentException;

    /**
     * Update PayIn
     *
     * This method checks the payment provider for updates of a specific PayIn.
     *
     * The controller action that handles the 3-D Secure validation redirect URL
     * will invoke this method for updating the specified PayIn. An update may
     * also be triggered by a webhook; hence the operation is implemented as
     * idempotent.
     *
     * @param payInKey The platform specific unique PayIn key
     * @param payInId The payment provider unique PayIn identifier
     * @return
     * @throws PaymentException
     */
    PayInDto updatePayIn(UUID payInKey, String payInId) throws PaymentException;

    /**
     * Transfer
     * @param command
     * @return
     * @throws PaymentException
     */
    TransferDto createTransfer(TransferCommandDto command) throws PaymentException;

    /**
     * Create a pay out from a provider's wallet to her bank account
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    PayOutDto createPayOut(PayOutCommandDto command) throws PaymentException;

}
