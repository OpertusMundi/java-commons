package eu.opertusmundi.common.model.pricing;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum EnumPricingModel {
    /**
     * Invalid pricing model
     */
    UNDEFINED(0),
    /*
     * Free
     */
    FREE(1),
    /**
     * Fixed payment model with or without updates
     */
    FIXED(2),
    /**
     * Buy once, pay per row with optional reverse block rate 
     */
    FIXED_PER_ROWS(3),
    /**
     * Buy once, pay based on population selection with optional reverse block rate
     */
    FIXED_FOR_POPULATION(4),
    /**
     * Pay per call, optional buy prepaid SKUs with reverse block rate
     */
    PER_CALL_WITH_PREPAID(5),
    /**
     * Pay per call, optional define reverse block rate pricing
     */
    PER_CALL_WITH_BLOCK_RATE(6),
    /**
     * Pay per row, optional buy prepaid SKUs with reverse block rate
     */
    PER_ROW_WITH_PREPAID(5),
    /**
     * Pay per row, optional define reverse block rate pricing
     */
    PER_ROW_WITH_BLOCK_RATE(6),
    ;

    private final int value;

    private EnumPricingModel(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static EnumPricingModel fromString(String value) {
        for (final EnumPricingModel item : EnumPricingModel.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumPricingModel.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumPricingModel> {

        @Override
        public EnumPricingModel deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumPricingModel.fromString(parser.getValueAsString());
        }
    }
}
