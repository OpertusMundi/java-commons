package eu.opertusmundi.common.service;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.ActivationTokenDto;
import eu.opertusmundi.common.model.account.CustomerProfessionalDto;
import eu.opertusmundi.common.model.account.EnumActivationTokenType;
import eu.opertusmundi.common.model.account.ProviderProfessionalCommandDto;
import eu.opertusmundi.common.model.analytics.ProfileRecord;
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.ActivationTokenRepository;
import eu.opertusmundi.common.util.BpmInstanceVariablesBuilder;

@Service
public class DefaultProviderRegistrationService extends AbstractCustomerRegistrationService implements ProviderRegistrationService {

    private static final String WORKFLOW_PROVIDER_REGISTRATION = "provider-registration";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private PersistentIdentifierService pidService;

    @Autowired(required = false)
    private ElasticSearchService elasticSearchService;

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
        ProcessInstanceDto instance = this.bpmEngine.findInstance(registrationKey);

        if (instance == null) {
            final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), userKey.toString())
                .variableAsString("userKey", userKey.toString())
                .variableAsString("registrationKey", registrationKey.toString())
                .variableAsBoolean("isUpdate", isUpdate)
                .variableAsBoolean("isReviewRequired", true)
                .build();

            this.bpmEngine.startProcessDefinitionByKey(
                WORKFLOW_PROVIDER_REGISTRATION, registrationKey.toString(), variables, true
            );
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
        final AccountEntity entity = this.accountRepository.findOneByKey(userKey).orElse(null);

        // The PID service user id will be set only during the first provider
        // registration
        final Integer pidServiceUserId = this.pidService.registerUser(entity.getProfile().getProviderRegistration().getName());

        final AccountDto account = this.accountRepository.completeProviderRegistration(userKey, pidServiceUserId);

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

        // Update account profile
        if (elasticSearchService != null) {
            elasticSearchService.addProfile(ProfileRecord.from(account));
        }

        return account;
    }

}
