package eu.opertusmundi.common.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import eu.opertusmundi.common.feign.client.BpmServerFeignClient;
import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.dto.ActivationTokenDto;
import feign.FeignException;

abstract class AbstractCustomerRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCustomerRegistrationService.class);

    @Autowired
    protected ObjectProvider<BpmServerFeignClient> bpmClient;

    protected ProcessInstanceDto findInstance(UUID businessKey) {
        try {
            final List<ProcessInstanceDto> instances = this.bpmClient.getObject().getInstance(businessKey.toString());

            return instances.stream().findFirst().orElse(null);
        } catch (final FeignException fex) {
            logger.error("[Feign Client] Operation has failed", fex);

            // Handle 404 errors as valid responses
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            throw new AssetDraftException(AssetMessageCode.BPM_SERVICE, "Operation on BPM server failed", fex);
        }
    }

    protected void setBooleanVariable(Map<String, VariableValueDto> variables, String name, Object value) {
        this.setVariable(variables, "Boolean", name, value);
    }

    protected void setStringVariable(Map<String, VariableValueDto> variables, String name, Object value) {
        this.setVariable(variables, "String", name, value);
    }

    protected void setVariable(Map<String, VariableValueDto> variables, String type, String name, Object value) {
        final VariableValueDto v = new VariableValueDto();

        v.setValue(value);
        v.setType(type);

        variables.put(name, v);
    }


    protected void sendMail(String name, ActivationTokenDto token) {
        // TODO: Implement
    }

}
