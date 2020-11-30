package eu.opertusmundi.common.model.converter;

import java.io.IOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.locationtech.spatial4j.io.jackson.ShapesAsGeoJSONModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;

@Converter(autoApply = false)
public class CatalogueItemCommandAttributeConverter implements AttributeConverter<CatalogueItemCommandDto, String> {

    private static final Logger logger = LoggerFactory.getLogger(CatalogueItemCommandAttributeConverter.class);

    private static ObjectMapper objectMapper;
    static {
        objectMapper = new ObjectMapper();

        objectMapper.registerModule(new ShapesAsGeoJSONModule());
        objectMapper.registerModule(new JavaTimeModule());

        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

        objectMapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    }

    @Override
    public String convertToDatabaseColumn(CatalogueItemCommandDto attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (final JsonProcessingException e) {
            logger.error("Failed to write process configuration", e);
        }
        return null;
    }

    @Override
    public CatalogueItemCommandDto convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<CatalogueItemCommandDto>() {
            });
        } catch (final IOException e) {
            logger.error("Failed to read process configuration", e);
        }
        return null;
    }

}