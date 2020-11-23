package eu.opertusmundi.common.service;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.EnumActivationTokenType;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.ActivationTokenDto;
import eu.opertusmundi.common.model.dto.ProviderProfessionalCommandDto;
import eu.opertusmundi.common.model.dto.CustomerProfessionalDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.ActivationTokenRepository;

@Service
public class DefaultProviderRegistrationService implements ProviderRegistrationService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private PaymentService paymentService;

    @Override
    @Transactional
    public AccountDto updateRegistration(ProviderProfessionalCommandDto command) {
        Assert.notNull(command, "Expected a non-null command");

        final AccountDto account = this.accountRepository.updateProviderRegistration(command);

        return account;
    }

    @Override
    @Transactional
    public AccountDto submitRegistration(ProviderProfessionalCommandDto command) {
        Assert.notNull(command, "Expected a non-null command");

        final AccountDto account         = this.accountRepository.submitProviderRegistration(command);
        final boolean    isUpdate        = account.getProfile().getProvider().getCurrent() != null;
        final UUID       userKey         = account.getKey();
        final UUID       registrationKey = account.getProfile().getProvider().getDraft().getKey();

        /*
         * TODO: Start workflow
         *
         * Workflow:
         * Create provider - If isUpdate is false
         * Update provider - If isUpdate is true
         *
         * Business Key:
         * The registration unique key
         *
         * Parameters:
         * User Key
         * Registration Key
         */
        if(isUpdate) {
            // Update workflow
            this.paymentService.updateUser(userKey, registrationKey);

            this.paymentService.updateBankAccount(userKey, registrationKey);

            this.completeRegistration(userKey, registrationKey);
        } else {
            // Create workflow
            this.paymentService.createUser(userKey, registrationKey);

            this.paymentService.createWallet(userKey, registrationKey);

            this.paymentService.createBankAccount(userKey, registrationKey);

            this.completeRegistration(userKey, registrationKey);
        }

        /*
         * Workflow end
         */

        return account;
    }

    @Override
    @Transactional
    public AccountDto cancelRegistration(UUID userKey) {
        final AccountDto account = this.accountRepository.cancelProviderRegistration(userKey);

        return account;
    }

    @Override
    @Transactional
    public AccountDto completeRegistration(UUID userKey, UUID registrationKey) {
        final AccountDto account = this.accountRepository.completeProviderRegistration(userKey, registrationKey);

        // Check if provider email requires validation
        final CustomerProfessionalDto provider = account.getProfile().getProvider().getCurrent();

        if (provider != null && !StringUtils.isBlank(provider.getEmail()) && !provider.isEmailVerified()) {
            // Create activation token
            final ActivationTokenDto token = this.activationTokenRepository.create(
                account.getId(), provider.getEmail(), 24, EnumActivationTokenType.PROVIDER
            );
            // Send activation link to client
            this.sendMail(account.getProfile().getFullName(), token);
        }

        return account;
    }

    private void sendMail(String name, ActivationTokenDto token) {
        // TODO: Implement
    }

}
