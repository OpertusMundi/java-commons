package eu.opertusmundi.common.feign.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.auth.BasicAuthRequestInterceptor;

public class BpmServerFeignClientConfiguration {

    @Value("${opertusmundi.feign.bpm-server.basic-auth.username}")
    private String username;

    @Value("${opertusmundi.feign.bpm-server.basic-auth.password}")
    private String password;

    @Bean
    public BasicAuthRequestInterceptor authInterceptor() {
        return new BasicAuthRequestInterceptor(this.username, this.password);
    }

}
