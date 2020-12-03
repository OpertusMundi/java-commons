package eu.opertusmundi.common.feign.client.config;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;

/**
 * Configure Ingest service feign client for handling file uploads
 *
 * @see <a href="https://github.com/OpenFeign/feign-form">Form Encoder</a>
 */
@Configuration
public class TransformServiceClientConfiguration {

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    public Encoder feignFormEncoder() {
        return new SpringFormEncoder(new SpringEncoder(this.messageConverters));
    }

}
