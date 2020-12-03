package eu.opertusmundi.common.model.transform;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import lombok.Getter;

public enum EnumTransformResponse {
    PROMPT("prompt"),
    DEFERRED("deferred"),
    ;

    @Getter
    private String value;

    private EnumTransformResponse(String value) {
        this.value = value;
    }

    public static EnumTransformResponse fromValue(String value) {
        for (final EnumTransformResponse e : EnumTransformResponse.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumTransformResponse]", value));
    }

    public static class Deserializer extends JsonDeserializer<EnumTransformResponse> {

        @Override
        public EnumTransformResponse deserialize(
            JsonParser parser, DeserializationContext context
        ) throws IOException, JsonProcessingException {
            final String value = parser.getValueAsString();

            // Check by name
            for (final EnumTransformResponse e : EnumTransformResponse.values()) {
                if (e.name().equals(value)) {
                    return e;
                }
            }

            // Check by value
            return EnumTransformResponse.fromValue(value);
        }

    }

}
