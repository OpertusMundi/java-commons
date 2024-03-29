package eu.opertusmundi.common.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.CustomerDraftProfessionalEntity;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.ActivationTokenDto;
import eu.opertusmundi.common.model.account.CustomerDraftDto;
import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.account.CustomerProfessionalDto;
import eu.opertusmundi.common.model.account.EnumActivationTokenType;
import eu.opertusmundi.common.model.account.ProviderProfessionalCommandDto;
import eu.opertusmundi.common.model.account.ProviderProfileCommandDto;
import eu.opertusmundi.common.model.analytics.ProfileRecord;
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.model.workflow.EnumWorkflow;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.ActivationTokenRepository;
import eu.opertusmundi.common.util.BpmInstanceVariablesBuilder;
import eu.opertusmundi.common.util.ImageUtils;
import eu.opertusmundi.common.util.TextUtils;

@Service
public class DefaultProviderRegistrationService extends AbstractCustomerRegistrationService implements ProviderRegistrationService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private PersistentIdentifierService pidService;

    @Autowired(required = false)
    private ElasticSearchService elasticSearchService;

    @Autowired
    private ImageUtils imageUtils;

    @Override
    @Transactional
    public AccountDto updateRegistration(ProviderProfessionalCommandDto command) throws IllegalArgumentException {
        Assert.notNull(command, "Expected a non-null command");

        command.setLogoImage(imageUtils.resizeImage(command.getLogoImage(), command.getLogoImageMimeType()));

        final AccountDto account = this.accountRepository.updateProviderRegistration(command);

        return account;
    }

    @Override
    @Transactional
    public AccountDto submitRegistration(ProviderProfessionalCommandDto command) throws IllegalArgumentException {
        Assert.notNull(command, "Expected a non-null command");

        command.setLogoImage(imageUtils.resizeImage(command.getLogoImage(), command.getLogoImageMimeType()));

        final AccountDto       account         = this.accountRepository.submitProviderRegistration(command);
        final CustomerDto      customer        = account.getProfile().getProvider().getCurrent();
        final CustomerDraftDto draft           = account.getProfile().getProvider().getDraft();
        final boolean          isUpdate        = customer != null;
        final UUID             registrationKey = draft.getKey();

        // Check if workflow exists
        final ProcessInstanceDto               instance         = this.bpmEngine.findInstance(registrationKey);
        final List<HistoricProcessInstanceDto> historyInstances = this.bpmEngine.findHistoryInstances(registrationKey);

        // Delete failed workflow instances
        if (instance == null && !StringUtils.isBlank(draft.getHelpdeskErrorMessage()) && !historyInstances.isEmpty()) {
            historyInstances.stream().forEach(i -> this.bpmEngine.deleteHistoryProcessInstance(i.getId()));
        }

        // Reset errors AFTER deleting failed workflow historic instances
        this.accountRepository.resetCustomerRegistrationErrors(command);

        if (instance == null) {
            final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), account.getKey().toString())
                .variableAsString("userId", account.getId().toString())
                .variableAsString("userKey", account.getKey().toString())
                .variableAsString("userName", account.getEmail())
                .variableAsString("registrationKey", registrationKey.toString())
                .variableAsBoolean("isUpdate", isUpdate)
                .variableAsBoolean("isReviewRequired", true)
                .build();

            this.bpmEngine.startProcessDefinitionByKey(
                EnumWorkflow.PROVIDER_REGISTRATION, registrationKey.toString(), variables, true
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
        final CustomerDraftProfessionalEntity draft            = entity.getProfile().getProviderRegistration();
        final String                          namespace        = TextUtils.slugify(draft.getName());
        final Integer                         pidServiceUserId = this.pidService.registerUser(draft.getName(), namespace);

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

    @Override
    @Transactional
    public AccountDto updateProfile(ProviderProfileCommandDto command) {
        Assert.notNull(command, "Expected a non-null command");

        command.setLogoImage(imageUtils.resizeImage(command.getLogoImage(), command.getLogoImageMimeType()));

        final AccountDto account = this.accountRepository.updateProviderProfile(command);

        return account;
    }

}
