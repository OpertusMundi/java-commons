package eu.opertusmundi.common.model.transform;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import lombok.Getter;

public enum EnumSourceType {
    VECTOR("vector"),
    RASTER("raster"),
    ;

    @Getter
    private String value;

    private EnumSourceType(String value) {
        this.value = value;
    }

    public static EnumSourceType fromValue(String value) {
        for (final EnumSourceType e : EnumSourceType.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumSourceType]", value));
    }

    public static class Deserializer extends JsonDeserializer<EnumSourceType> {

        @Override
        public EnumSourceType deserialize(
            JsonParser parser, DeserializationContext context
        ) throws IOException, JsonProcessingException {
            return EnumSourceType.fromValue(parser.getValueAsString());
        }

    }

}
