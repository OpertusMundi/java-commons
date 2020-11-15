package eu.opertusmundi.common.service;

import java.util.UUID;

import eu.opertusmundi.common.model.dto.AccountDto;

public interface  PaymentService {

    /**
     * Create a new user in the external payment service and link it to a
     * OpertusMundi account
     *
     * @param userKey
     * @param registrationKey
     * @return
     */
    AccountDto createUser(UUID userKey, UUID registrationKey);

    /**
     * Update an existing user in the external payment service. The account must
     * already be lined to a OpertusMundi account
     *
     * @param userKey
     * @param registrationKey
     * @return
     */
    AccountDto updateUser(UUID userKey, UUID registrationKey);

    /**
     * Creates a new waller in the external payment service and links it to a
     * OpertusMundi account
     *
     * @param userKey
     * @param registrationKey
     * @return
     */
    AccountDto createWallet(UUID userKey, UUID registrationKey);

    /**
     * Create a new bank account in the external payment service and links it to
     * a OpertusMundi account
     *
     * @param userKey
     * @param registrationKey
     * @return
     */
    AccountDto createBankAccount(UUID userKey, UUID registrationKey);

    /**
     * Update an existing bank account in the external payment service.
     *
     * @param userKey
     * @param registrationKey
     * @return
     */
    AccountDto updateBankAccount(UUID userKey, UUID registrationKey);

}
