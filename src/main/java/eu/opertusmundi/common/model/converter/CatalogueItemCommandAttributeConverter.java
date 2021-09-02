package eu.opertusmundi.common.model.converter;

import java.io.IOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.locationtech.spatial4j.io.jackson.ShapesAsGeoJSONModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.service.AssetDraftException;

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

        // Ignore unknown properties to prevent existing process instances from
        // failing e.g. if a new metadata property is added after Helpdesk
        // validation but before provider acceptance
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public String convertToDatabaseColumn(CatalogueItemCommandDto attribute) {
        try {
            Assert.notNull(attribute, "Expected a non-null attribute");

            return objectMapper.writeValueAsString(attribute);
        } catch (final JsonProcessingException ex) {
            final String message = String.format("Failed to serialize object. [type=%s]", CatalogueItemCommandDto.class);
            logger.error(message, ex);

            throw new AssetDraftException(AssetMessageCode.SERIALIZATION_ERROR, message, ex);
        }
    }

    @Override
    public CatalogueItemCommandDto convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<CatalogueItemCommandDto>() { });
        } catch (final IOException ex) {
            final String message = String.format("Failed to deserialize string. [data=%s, type=%s]", dbData, CatalogueItemCommandDto.class);
            logger.error(message, ex);

            throw new AssetDraftException(AssetMessageCode.SERIALIZATION_ERROR, message, ex);
        }
    }

}