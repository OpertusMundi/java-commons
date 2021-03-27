package eu.opertusmundi.common.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;

import com.mangopay.MangoPayApi;

public abstract class BaseMangoPayService {

    @Value("${opertusmundi.payments.mangopay.base-url:}")
    private String baseUrl;

    @Value("${opertusmundi.payments.mangopay.client-id:}")
    private String clientId;

    @Value("${opertusmundi.payments.mangopay.client-password:}")
    private String clientPassword;
    
    protected MangoPayApi api;
    
    @PostConstruct
    private void init() {
        this.api = new MangoPayApi();

        this.api.getConfig().setBaseUrl(this.baseUrl);
        this.api.getConfig().setClientId(this.clientId);
        this.api.getConfig().setClientPassword(this.clientPassword);
    }
    
}
