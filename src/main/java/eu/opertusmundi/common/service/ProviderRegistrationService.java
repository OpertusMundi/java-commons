package eu.opertusmundi.common.service;

import java.util.UUID;

import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.ProviderProfessionalCommandDto;
import eu.opertusmundi.common.model.account.ProviderProfileCommandDto;

public interface ProviderRegistrationService {

    /**
     * Update provider draft data
     *
     * @param command The provider update command
     * @return
     */
    AccountDto updateRegistration(ProviderProfessionalCommandDto command);

    /**
     * Submit provider data for updating user profile
     *
     * @param command The provider update command
     * @return
     */
    AccountDto submitRegistration(ProviderProfessionalCommandDto command);

    /**
     * Cancel pending provider registration request
     *
     * @param userKey
     * @return
     */
    AccountDto cancelRegistration(UUID userKey);

    /**
     * Complete provider registration and update user profile
     *
     * @param userKey
     * @return
     */
    AccountDto completeRegistration(UUID userKey);

    /**
     * Update provider profile
     *
     * @param command The profile update command
     * @return
     */
    AccountDto updateProfile(ProviderProfileCommandDto command);

}
