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

import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.ActivationTokenDto;
import eu.opertusmundi.common.model.account.ConsumerCommandDto;
import eu.opertusmundi.common.model.account.ConsumerProfessionalCommandDto;
import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.account.EnumActivationTokenType;
import eu.opertusmundi.common.model.account.EnumMangopayUserType;
import eu.opertusmundi.common.model.analytics.ProfileRecord;
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.model.workflow.EnumWorkflow;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.ActivationTokenRepository;
import eu.opertusmundi.common.util.BpmInstanceVariablesBuilder;
import eu.opertusmundi.common.util.ImageUtils;

@Service
public class DefaultConsumerRegistrationService extends AbstractCustomerRegistrationService implements ConsumerRegistrationService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired(required = false)
    private ElasticSearchService elasticSearchService;

    @Autowired
    private ImageUtils imageUtils;

    @Override
    @Transactional
    public AccountDto updateRegistration(ConsumerCommandDto command) throws IllegalArgumentException {
        Assert.notNull(command, "Expected a non-null command");

        if (command.getType() == EnumMangopayUserType.PROFESSIONAL) {
            final ConsumerProfessionalCommandDto consumerCommand = (ConsumerProfessionalCommandDto) command;
            consumerCommand.setLogoImage(imageUtils.resizeImage(consumerCommand.getLogoImage(), consumerCommand.getLogoImageMimeType()));
        }

        final AccountDto account = this.accountRepository.updateConsumerRegistration(command);

        return account;
    }

    @Override
    @Transactional
    public AccountDto submitRegistration(ConsumerCommandDto command) throws IllegalArgumentException {
        Assert.notNull(command, "Expected a non-null command");

        if (command.getType() == EnumMangopayUserType.PROFESSIONAL) {
            final ConsumerProfessionalCommandDto consumerCommand = (ConsumerProfessionalCommandDto) command;
            consumerCommand.setLogoImage(imageUtils.resizeImage(consumerCommand.getLogoImage(), consumerCommand.getLogoImageMimeType()));
        }

        final AccountDto account         = this.accountRepository.submitConsumerRegistration(command);
        final boolean    isUpdate        = account.getProfile().getConsumer().getCurrent() != null;
        final UUID       userKey         = account.getKey();
        final UUID       registrationKey = account.getProfile().getConsumer().getDraft().getKey();

        // Check if workflow exists
        final ProcessInstanceDto instance = this.bpmEngine.findInstance(registrationKey.toString());

        if (instance == null) {
            final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), userKey.toString())
                .variableAsString("userKey", userKey.toString())
                .variableAsString("registrationKey", registrationKey.toString())
                .variableAsBoolean("isUpdate", isUpdate)
                .variableAsBoolean("isReviewRequired", true)
                .build();

            this.bpmEngine.startProcessDefinitionByKey(
                EnumWorkflow.CONSUMER_REGISTRATION, registrationKey.toString(), variables, true
            );
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

        // Update account profile
        if (elasticSearchService != null) {
            elasticSearchService.addProfile(ProfileRecord.from(account));
        }

        return account;
    }

}
