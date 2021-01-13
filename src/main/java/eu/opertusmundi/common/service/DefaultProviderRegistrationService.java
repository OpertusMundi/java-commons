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
import eu.opertusmundi.common.model.dto.CustomerProfessionalDto;
import eu.opertusmundi.common.model.dto.ProviderProfessionalCommandDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.ActivationTokenRepository;

@Service
public class DefaultProviderRegistrationService extends AbstractCustomerRegistrationService implements ProviderRegistrationService {

    private static final String WORKFLOW_PROVIDER_REGISTRATION = "provider-registration";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Override
    @Transactional
    public AccountDto updateRegistration(ProviderProfessionalCommandDto command) throws IllegalArgumentException {
        Assert.notNull(command, "Expected a non-null command");

        final AccountDto account = this.accountRepository.updateProviderRegistration(command);

        return account;
    }

    @Override
    @Transactional
    public AccountDto submitRegistration(ProviderProfessionalCommandDto command) throws IllegalArgumentException {
        Assert.notNull(command, "Expected a non-null command");

        final AccountDto account         = this.accountRepository.submitProviderRegistration(command);
        final boolean    isUpdate        = account.getProfile().getProvider().getCurrent() != null;
        final UUID       userKey         = account.getKey();
        final UUID       registrationKey = account.getProfile().getProvider().getDraft().getKey();

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

            instance = this.bpmClient.getObject().startProcessByKey(WORKFLOW_PROVIDER_REGISTRATION, options);
        }

        return account;
    }

    @Override
    @Transactional
    public AccountDto cancelRegistration(UUID userKey) throws IllegalArgumentException {
        final AccountDto account = this.accountRepository.cancelProviderRegistration(userKey);

        return account;
    }

    @Override
    @Transactional
    public AccountDto completeRegistration(UUID userKey) throws IllegalArgumentException {
        final AccountDto account = this.accountRepository.completeProviderRegistration(userKey);

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

}
