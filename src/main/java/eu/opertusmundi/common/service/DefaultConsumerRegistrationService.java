package eu.opertusmundi.common.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
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
public class DefaultConsumerRegistrationService extends AbstractCustomerRegistrationService implements ConsumerRegistrationService {

    private static final String WORKFLOW_CONSUMER_REGISTRATION = "consumer-registration";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Override
    @Transactional
    public AccountDto updateRegistration(CustomerCommandDto command) throws IllegalArgumentException {
        Assert.notNull(command, "Expected a non-null command");

        final AccountDto account = this.accountRepository.updateConsumerRegistration(command);

        return account;
    }

    @Override
    @Transactional
    public AccountDto submitRegistration(CustomerCommandDto command) throws IllegalArgumentException {
        Assert.notNull(command, "Expected a non-null command");

        final AccountDto account         = this.accountRepository.submitConsumerRegistration(command);
        final boolean    isUpdate        = account.getProfile().getConsumer().getCurrent() != null;
        final UUID       userKey         = account.getKey();
        final UUID       registrationKey = account.getProfile().getConsumer().getDraft().getKey();

        // Check if workflow exists
        ProcessInstanceDto instance = this.findInstance(registrationKey);

        if (instance == null) {
            final StartProcessInstanceDto options = new StartProcessInstanceDto();

            final Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();

            // Set variables
            this.setStringVariable(variables, "userKey", userKey);
            this.setStringVariable(variables, "registrationKey", registrationKey);
            this.setBooleanVariable(variables, "isUpdate", isUpdate);

            options.setBusinessKey(registrationKey.toString());
            options.setVariables(variables);
            options.setWithVariablesInReturn(true);

            instance = this.bpmClient.getObject().startProcessByKey(WORKFLOW_CONSUMER_REGISTRATION, options);
        }

        return account;
    }

    @Override
    @Transactional
    public AccountDto cancelRegistration(UUID userKey) throws IllegalArgumentException {
        final AccountDto account = this.accountRepository.cancelConsumerRegistration(userKey);

        return account;
    }

    @Override
    @Transactional
    public AccountDto completeRegistration(UUID userKey) throws IllegalArgumentException {
        final AccountDto account = this.accountRepository.completeConsumerRegistration(userKey);

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

}
