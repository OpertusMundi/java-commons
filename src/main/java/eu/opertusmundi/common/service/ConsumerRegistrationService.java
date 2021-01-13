package eu.opertusmundi.common.service;

import java.util.UUID;

import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.CustomerCommandDto;

public interface ConsumerRegistrationService {

    /**
     * Update consumer draft data
     *
     * @param command The consumer update command
     * @return
     */
    AccountDto updateRegistration(CustomerCommandDto command);

    /**
     * Submit consumer data for updating user profile
     *
     * @param command The consumer update command
     * @return
     */
    AccountDto submitRegistration(CustomerCommandDto command);


    /**
     * Cancel pending consumer registration request
     *
     * @param userKey
     * @return
     */
    AccountDto cancelRegistration(UUID userKey);

    /**
     * Complete consumer registration and update user profile
     *
     * @param userKey
     * @return
     */
    AccountDto completeRegistration(UUID userKey);

}
