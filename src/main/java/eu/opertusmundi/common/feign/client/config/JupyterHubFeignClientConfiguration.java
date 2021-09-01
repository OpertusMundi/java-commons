package eu.opertusmundi.common.feign.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class JupyterHubFeignClientConfiguration 
{
    @Value("${opertusmundi.feign.jupyterhub.access-token}")
    private String accessToken;
    
    @Bean
    public RequestInterceptor authRequestInterceptor()
    {
        return new RequestInterceptor() {
            
            @Override
            public void apply(RequestTemplate requestTemplate) {
                requestTemplate.header(HttpHeaders.AUTHORIZATION, "token" + " " + accessToken);
            }
        };
    }
}
