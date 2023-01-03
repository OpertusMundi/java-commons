package eu.opertusmundi.common.service.mangopay;

import java.util.UUID;

import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.UserBlockedStatusDto;
import eu.opertusmundi.common.model.payment.UserRegistrationCommand;

public interface UserService {

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

}
