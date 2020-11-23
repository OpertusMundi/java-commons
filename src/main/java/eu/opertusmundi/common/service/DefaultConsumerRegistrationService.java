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
import eu.opertusmundi.common.model.dto.CustomerCommandDto;
import eu.opertusmundi.common.model.dto.CustomerDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.ActivationTokenRepository;

@Service
public class DefaultConsumerRegistrationService implements ConsumerRegistrationService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private PaymentService paymentService;

    @Override
    @Transactional
    public AccountDto updateRegistration(CustomerCommandDto command) {
        Assert.notNull(command, "Expected a non-null command");

        final AccountDto account = this.accountRepository.updateConsumerRegistration(command);

        return account;
    }

    @Override
    @Transactional
    public AccountDto submitRegistration(CustomerCommandDto command) {
        Assert.notNull(command, "Expected a non-null command");

        final AccountDto account         = this.accountRepository.submitConsumerRegistration(command);
        final boolean    isUpdate        = account.getProfile().getConsumer().getCurrent() != null;
        final UUID       userKey         = account.getKey();
        final UUID       registrationKey = account.getProfile().getConsumer().getDraft().getKey();

        /*
         * TODO: Start workflow
         *
         * Workflow:
         * Create consumer - If isUpdate is false
         * Update consumer - If isUpdate is true
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

            this.completeRegistration(userKey, registrationKey);
        } else {
            // Create workflow
            this.paymentService.createUser(userKey, registrationKey);

            this.paymentService.createWallet(userKey, registrationKey);

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
        final AccountDto account = this.accountRepository.cancelConsumerRegistration(userKey);

        return account;
    }

    @Override
    @Transactional
    public AccountDto completeRegistration(UUID userKey, UUID registrationKey) {
        final AccountDto account = this.accountRepository.completeConsumerRegistration(userKey, registrationKey);

        // Check if consumer email requires validation
        final CustomerDto consumer = account.getProfile().getConsumer().getCurrent();

        if (consumer != null && !StringUtils.isBlank(consumer.getEmail()) && !consumer.isEmailVerified()) {
            // Create activation token
            final ActivationTokenDto token = this.activationTokenRepository.create(
                account.getId(), consumer.getEmail(), 24, EnumActivationTokenType.CONSUMER
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
