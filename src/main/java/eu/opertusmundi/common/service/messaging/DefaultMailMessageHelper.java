package eu.opertusmundi.common.service.messaging;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.MessageFormat;

import eu.opertusmundi.common.domain.MailTemplateEntity;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.email.EmailAddressDto;
import eu.opertusmundi.common.model.email.EnumMailType;
import eu.opertusmundi.common.model.email.MailMessageCode;
import eu.opertusmundi.common.repository.MailTemplateRepository;
import io.jsonwebtoken.lang.Assert;

@Service
public class DefaultMailMessageHelper implements MailMessageHelper {

    @Autowired
    private MailTemplateRepository templateRepository;

    @Override
    public String composeSubject(EnumMailType type, Map<String, Object> variables) {
        Assert.isTrue(type != null, "Expected a non-null mail type");

        final MailTemplateEntity template = templateRepository.findOneByType(type).orElse(null);
        if (template == null) {
            throw new ServiceException(MailMessageCode.TEMPLATE_NOT_FOUND, String.format("Mail template %s was not found", type));
        }

        switch (type) {
            case ACCOUNT_ACTIVATION_TOKEN :
                return MessageFormat.format(template.getSubjectTemplate(), variables);
        }

        throw new ServiceException(MailMessageCode.TYPE_NOT_SUPPORTED, String.format("Mail type %s is not supported", type));
    }

    @Override
    public String resolveTemplate(EnumMailType type, Map<String, Object> variables) {
        Assert.isTrue(type != null, "Expected a non-null mail type");

        final MailTemplateEntity template = templateRepository.findOneByType(type).orElse(null);
        if (template == null) {
            throw new ServiceException(MailMessageCode.TEMPLATE_NOT_FOUND, String.format("Mail template %s was not found", type));
        }

        return template.getContentTemplate();
    }

    @Override
    public EmailAddressDto getSender(EnumMailType type, Map<String, Object> variables) {
        Assert.isTrue(type != null, "Expected a non-null mail type");

        final MailTemplateEntity template = templateRepository.findOneByType(type).orElse(null);
        if (template == null) {
            throw new ServiceException(MailMessageCode.TEMPLATE_NOT_FOUND, String.format("Mail template %s was not found", type));
        }

        return EmailAddressDto.of(template.getSenderEmail(), template.getSenderName());
    }

    @Override
    public Map<String, Object> createModel(EnumMailType type, Map<String, Object> variables) {
        Assert.isTrue(type != null, "Expected a non-null mail type");

        final Map<String, Object> result = new HashMap<>();
        if (variables != null) {
            result.putAll(variables);
        }

        switch (type) {
            case ACCOUNT_ACTIVATION_TOKEN :
                // Add any extra variables here ...
                break;
        }

        return result;
    }

}
