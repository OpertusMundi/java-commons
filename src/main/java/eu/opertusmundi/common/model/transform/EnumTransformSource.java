package eu.opertusmundi.common.model.transform;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import lombok.Getter;

public enum EnumTransformSource {
    VECTOR("vector"),
    RASTER("raster"),
    ;

    @Getter
    private String value;

    private EnumTransformSource(String value) {
        this.value = value;
    }

    public static EnumTransformSource fromValue(String value) {
        for (final EnumTransformSource e : EnumTransformSource.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumTransformSource]", value));
    }

    public static class Deserializer extends JsonDeserializer<EnumTransformSource> {

        @Override
        public EnumTransformSource deserialize(
            JsonParser parser, DeserializationContext context
        ) throws IOException, JsonProcessingException {
            return EnumTransformSource.fromValue(parser.getValueAsString());
        }

    }

}
