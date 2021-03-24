package eu.opertusmundi.common.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.BankAccountDto;
import eu.opertusmundi.common.model.order.CartDto;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.model.payment.BankwirePayInCommand;
import eu.opertusmundi.common.model.payment.CardDirectPayInCommand;
import eu.opertusmundi.common.model.payment.CardDto;
import eu.opertusmundi.common.model.payment.CardRegistrationCommandDto;
import eu.opertusmundi.common.model.payment.CardRegistrationDto;
import eu.opertusmundi.common.model.payment.ClientDto;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.PayOutCommandDto;
import eu.opertusmundi.common.model.payment.PayOutDto;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.TransferCommandDto;
import eu.opertusmundi.common.model.payment.TransferDto;
import eu.opertusmundi.common.model.payment.UserCardCommandDto;
import eu.opertusmundi.common.model.payment.UserCommandDto;
import eu.opertusmundi.common.model.payment.UserPaginationCommandDto;
import eu.opertusmundi.common.model.payment.UserRegistrationCommandDto;

public interface  PaymentService {

    /**
     * Creates a new order from a cart
     * 
     * @param cart
     * @return
     * @throws PaymentException
     */
    OrderDto createOrderFromCart(CartDto cart) throws PaymentException;
    
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
    AccountDto createUser(UserRegistrationCommandDto command) throws PaymentException;

    /**
     * Update an existing user in the external payment service. The account must
     * already be lined to a OpertusMundi account
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    AccountDto updateUser(UserRegistrationCommandDto command) throws PaymentException;

    /**
     * Creates a new waller in the external payment service and links it to a
     * OpertusMundi account
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    AccountDto createWallet(UserRegistrationCommandDto command) throws PaymentException;

    /**
     * Create a new bank account in the external payment service and links it to
     * a OpertusMundi account
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    AccountDto createBankAccount(UserRegistrationCommandDto command) throws PaymentException;

    /**
     * Update an existing bank account in the external payment service.
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    AccountDto updateBankAccount(UserRegistrationCommandDto command) throws PaymentException;

    /**
     * Get user bank accounts.
     * 
     * MangoPay pagination is 1-based.
     * 
     * @param command
     * @return
     * @throws PaymentException
     */
    List<BankAccountDto> getBankAccounts(UserPaginationCommandDto command) throws PaymentException;
    
    /**
     * Get user registered cards
     * 
     * MangoPay pagination is 1-based.
     * 
     * @param command
     * @return
     * @throws PaymentException
     */
    List<CardDto> getRegisteredCards(UserPaginationCommandDto command) throws PaymentException;
    
    /**
     * Get user registered card
     * 
     * @param command
     * @return
     * @throws PaymentException
     */
    CardDto getRegisteredCard(UserCardCommandDto command) throws PaymentException;
    
    /**
     * Deactivate card
     * 
     * @param command
     * @throws PaymentException
     */
    void deactivateCard(UserCardCommandDto command) throws PaymentException;
    
    /**
     * Create card registration
     * 
     * @param command
     * @return
     * @throws PaymentException
     */
    CardRegistrationDto createCardRegistration(UserCommandDto command) throws PaymentException;
    
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

    /**
     * Handles payment provider webhook event
     * 
     * @param eventType
     * @param resourceId
     * @param date
     * @throws PaymentException
     */
    void handleWebHook(String eventType, String resourceId, ZonedDateTime date) throws PaymentException;
    
}
