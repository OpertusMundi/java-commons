package eu.opertusmundi.common.model.converter;

import java.io.IOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.spatial4j.io.jackson.ShapesAsGeoJSONModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Converter(autoApply = false)
public class JsonNodeAttributeConverter implements AttributeConverter<JsonNode, String> {

    private static final Logger logger = LoggerFactory.getLogger(JsonNodeAttributeConverter.class);

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
    public String convertToDatabaseColumn(JsonNode attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.toString();
    }

    @Override
    public JsonNode convertToEntityAttribute(String dbData) {
        if (StringUtils.isBlank(dbData)) {
            return null;
        }
        try {
            return objectMapper.readTree(dbData);
        } catch (final IOException e) {
            logger.error("Failed to read JSON data", e);
        }
        return null;
    }

}