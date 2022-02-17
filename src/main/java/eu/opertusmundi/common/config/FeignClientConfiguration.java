package eu.opertusmundi.common.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import eu.opertusmundi.common.feign.client.config.DefaultFeignClientConfiguration;

@Configuration
@EnableFeignClients(
    basePackageClasses = {
        eu.opertusmundi.common.feign.client._Marker.class,
    },
    defaultConfiguration = DefaultFeignClientConfiguration.class
)
@PropertySource("classpath:config/feign-client.properties")
public class FeignClientConfiguration {

    // Add any custom bean definitions here

}
