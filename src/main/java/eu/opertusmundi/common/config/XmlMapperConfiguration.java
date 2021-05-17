package eu.opertusmundi.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Configuration
public class XmlMapperConfiguration {

    @Bean({"xmlMapper", "defaultXmlMapper"})
    public XmlMapper defaultXmlMapper() {
        final XmlMapper xmlMapper = new XmlMapper();

        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return xmlMapper;
    }

}