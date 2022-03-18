package eu.opertusmundi.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import eu.opertusmundi.common.util.ViesVatClient;

@Configuration
public class WebServiceClientConfiguration {

    @Bean
    public Jaxb2Marshaller viesMarshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // This package must match the package in the <generatePackage>
        // specified in pom.xml
        marshaller.setContextPath("eu.europa.ec.taxation.wsdl");
        return marshaller;
    }

    @Bean
    public ViesVatClient viesVatClient(@Qualifier("viesMarshaller") Jaxb2Marshaller marshaller) {
        final ViesVatClient client = new ViesVatClient();
        client.setDefaultUri("https://ec.europa.eu/taxation_customs/vies/services/checkVatService");
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        return client;
    }

}
