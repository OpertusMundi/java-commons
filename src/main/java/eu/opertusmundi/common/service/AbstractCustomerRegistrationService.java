package eu.opertusmundi.common.service;

import org.springframework.beans.factory.annotation.Autowired;

import eu.opertusmundi.common.model.account.ActivationTokenDto;
import eu.opertusmundi.common.util.BpmEngineUtils;

abstract class AbstractCustomerRegistrationService {

    @Autowired
    protected BpmEngineUtils bpmEngine;

    protected void sendMail(String name, ActivationTokenDto token) {
        // TODO: Implement
    }

}
