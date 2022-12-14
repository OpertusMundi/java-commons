package eu.opertusmundi.common.service.mangopay;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.Nullable;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.BankAccountDto;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.location.Location;
import eu.opertusmundi.common.model.order.CartDto;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.model.payment.BankwirePayInCommand;
import eu.opertusmundi.common.model.payment.CardDirectPayInCommand;
import eu.opertusmundi.common.model.payment.CardDto;
import eu.opertusmundi.common.model.payment.CardRegistrationCommandDto;
import eu.opertusmundi.common.model.payment.CardRegistrationDto;
import eu.opertusmundi.common.model.payment.CheckoutServiceBillingCommandDto;
import eu.opertusmundi.common.model.payment.ClientDto;
import eu.opertusmundi.common.model.payment.ClientWalletDto;
import eu.opertusmundi.common.model.payment.EnumPayInItemSortField;
import eu.opertusmundi.common.model.payment.EnumPayInSortField;
import eu.opertusmundi.common.model.payment.EnumPayOutSortField;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.EventDto;
import eu.opertusmundi.common.model.payment.FreePayInCommand;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.PayOutCommandDto;
import eu.opertusmundi.common.model.payment.PayOutDto;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.TransferDto;
import eu.opertusmundi.common.model.payment.UserBlockedStatusDto;
import eu.opertusmundi.common.model.payment.UserCardCommand;
import eu.opertusmundi.common.model.payment.UserCommand;
import eu.opertusmundi.common.model.payment.UserPaginationCommand;
import eu.opertusmundi.common.model.payment.UserRegistrationCommand;
import eu.opertusmundi.common.model.payment.WalletDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerPayInDto;
import eu.opertusmundi.common.model.payment.provider.ProviderPayInItemDto;

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
     * Initializes a new PayIn from a list of subscription billing record keys
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    PayInDto preparePayInFromServiceBillingRecords(CheckoutServiceBillingCommandDto command) throws PaymentException;

    /**
     * Get client registration information
     *
     * @return
     * @throws PaymentException
     */
    ClientDto getClient() throws PaymentException;

    /**
     * Get client wallets
     *
     * @return
     * @throws PaymentException
     */
    List<ClientWalletDto> getClientWallets() throws PaymentException;

    /**
     * Get payment provider events
     *
     * @param days
     * @return
     * @throws PaymentException
     */
    List<EventDto> getEvents(int days) throws PaymentException;

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
    List<CardDto> getCardRegistrations(UserPaginationCommand command) throws PaymentException;

    /**
     * Get user registered card
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    CardDto getCardRegistration(UserCardCommand command) throws PaymentException;

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
     * Get consumer PayIn by key
     *
     * @param userId
     * @param payInKey
     * @return
     */
    PayInDto getConsumerPayIn(Integer userId, UUID payInKey);

    /**
     * Get provider PayIn by key
     *
     * @param userId
     * @param payInKey
     * @param index
     * @return
     */
    ProviderPayInItemDto getProviderPayInItem(Integer userId, UUID payInKey, Integer index);

    /**
     * Query payment provider service for PayIn transaction status
     *
     * @param payIn
     * @return
     * @throws PaymentException
     */
    EnumTransactionStatus getPayInStatus(String payIn) throws PaymentException;

    /**
     * Refreshes all user wallets from remote payment provider
     *
     * @param userKey
     * @return
     * @throws PaymentException
     */
    AccountDto refreshUserWallets(UUID userKey) throws PaymentException;

    /**
     * Updates wallet funds for the specified customer from the Payment Provider
     * (MANGOPAY)
     *
     * @param userKey
     * @param type
     * @return
     * @throws PaymentException
     */
    WalletDto updateCustomerWalletFunds(UUID userKey, EnumCustomerType type) throws PaymentException;

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
    PageResultDto<ConsumerPayInDto> findAllConsumerPayIns(
        UUID userKey, EnumTransactionStatus status, int pageIndex, int pageSize, EnumPayInSortField orderBy, EnumSortingOrder order
    );

    /**
     * Search provider PayIns
     *
     * @param userKey
     * @param status
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    PageResultDto<ProviderPayInItemDto> findAllProviderPayInItems(
        UUID userKey, EnumTransactionStatus status, int pageIndex, int pageSize, EnumPayInItemSortField orderBy, EnumSortingOrder order
    );

    /**
     * Create a free PayIn for consumer order
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    PayInDto createPayInFreeForOrder(FreePayInCommand command) throws PaymentException;

    /**
     * Create bank wire PayIn for a consumer order
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    PayInDto createPayInBankwireForOrder(BankwirePayInCommand command) throws PaymentException;

    /**
     * Create a direct card PayIn with a registered card for the specified order
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    PayInDto createPayInCardDirectForOrder(CardDirectPayInCommand command) throws PaymentException;

    /**
     * Updates a direct card PayIn with a registered card for a collection of
     * subscription billing records
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    PayInDto updatePayInCardDirectForSubscriptions(CardDirectPayInCommand command) throws PaymentException;

    /**
     * Updates the PayIn status in a PayIn workflow instance
     *
     * @param payInId
     * @throws PaymentException
     */
    default void updateWorkflowInstancePayInStatus(String payInId) throws PaymentException {
        this.updateWorkflowInstancePayInStatus(null, payInId);
    }

    /**
     * Updates the PayIn status in a PayIn workflow instance
     *
     * @param payInKey
     * @param payInId
     * @throws PaymentException
     */
    void updateWorkflowInstancePayInStatus(UUID payInKey, String payInId) throws PaymentException;

    /**
     * Update PayIn
     *
     * This method checks the payment provider for updates of a specific PayIn.
     * A webhook may invoke this method to update a pending bankwire or credit
     * card PyaIn.
     *
     * @param payInId The payment provider unique PayIn identifier
     * @return
     * @throws PaymentException
     */
    default PayInDto updatePayIn(String payInId) throws PaymentException {
        return this.updatePayIn(null, payInId);
    }

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
    PayInDto updatePayIn(@Nullable UUID payInKey, String payInId) throws PaymentException;

    /**
     * Create transfers for all items in a PayIn record
     *
     * @param userKey
     * @param payInKey
     * @return
     * @throws PaymentException
     */
    List<TransferDto> createTransfer(UUID userKey, UUID payInKey) throws PaymentException;

    /**
     * Update transfer
     *
     * This method checks the payment provider for updates of a specific
     * Transfer. A webhook may invoke this method to update a pending transfer
     * with the execution date and its status.
     *
     * @param transfer The payment provider Transfer unique identifier
     * @throws PaymentException
     */
    void updateTransfer(String transferId) throws PaymentException;

    /**
     * Create a PayOut record from a provider's wallet to her bank account in
     * the OpertusMundi platform.
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    PayOutDto createPayOutAtOpertusMundi(PayOutCommandDto command) throws PaymentException;

    /**
     * Create a PayOut at the MANGOPAY service
     *
     * @param payOutKey
     * @return
     * @throws PaymentException
     */
    PayOutDto createPayOutAtProvider(UUID payOutKey) throws PaymentException;

    /**
     * Sends a message to a PayOut process instance to update its status
     *
     * @param payOutId
     * @throws PaymentException
     */
    void sendPayOutStatusUpdateMessage(String payOutId) throws PaymentException;

    /**
     * Update PayOut
     *
     * @param payOutKey PayOut unique OpertusMundi key
     * @param payOutId The payment provider unique PayOut identifier
     * @return
     * @throws PaymentException
     */
    PayOutDto updatePayOut(UUID payOutKey, String payOutId) throws PaymentException;

    /**
     * Update PayOut refund
     *
     * @param refundId
     * @return
     * @throws PaymentException
     */
    PayOutDto updatePayOutRefund(String refundId) throws PaymentException;

    /**
     * Get provider PayOut by key
     *
     * @param userId
     * @param payOutKey
     * @return
     */
    PayOutDto getProviderPayOut(Integer userId, UUID payOutKey);

    /**
     * Search provider PayOuts
     *
     * @param userKey
     * @param status
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    PageResultDto<PayOutDto> findAllProviderPayOuts(
        UUID userKey, EnumTransactionStatus status, int pageIndex, int pageSize, EnumPayOutSortField orderBy, EnumSortingOrder order
    );

    /**
     * Get user block status
     *
     * @param userKey
     * @return
     * @throws PaymentException
     */
    UserBlockedStatusDto getUserBlockStatus(UUID userKey) throws PaymentException;

    /**
     * Updates user block status
     *
     * @param providerUserId
     * @throws PaymentException
     */
    void updateUserBlockStatus(String providerUserId) throws PaymentException;

    /**
     * Updates user block status
     *
     * @param userKey
     * @param type
     * @throws PaymentException
     */
    void updateUserBlockStatus(UUID userKey, EnumCustomerType type) throws PaymentException;

}
