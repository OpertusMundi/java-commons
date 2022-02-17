package eu.opertusmundi.common.feign.client.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import feign.Client;
import feign.httpclient.ApacheHttpClient;

public class DefaultFeignClientConfiguration {

    @Autowired
    private CloseableHttpClient httpClient;

    @Bean
    public Client client() {
        return new ApacheHttpClient(this.httpClient);
    }

}
