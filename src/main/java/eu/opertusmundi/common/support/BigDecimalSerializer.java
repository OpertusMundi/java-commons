package eu.opertusmundi.common.support;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom serializer for BigDecimal values
 *
 * When the scale of a value is 0, the fractional part is not serialized e.g.
 * number 25 is written as <code>25</code> instead of <code>25.0</code>. This
 * results in type conflicts when indexing JSON documents in Elasticsearch where
 * a property mapping may be of type float.
 */
public class BigDecimalSerializer extends JsonSerializer<BigDecimal> {

    @Override
    public void serialize(
        final BigDecimal value, final JsonGenerator jgen, final SerializerProvider provider
    ) throws IOException, JsonProcessingException {
        if (value == null) {
            jgen.writeNull();
        } else {
            if (value.scale() == 0) {
                jgen.writeRawValue(value.toPlainString() + ".0");
            } else {
                jgen.writeRawValue(value.toPlainString());
            }

        }
    }

}