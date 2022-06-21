package eu.opertusmundi.common.model.pricing;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(enumAsRef = true)
public enum EnumPricingModel {
    // Topio pricing models

    /**
     * Invalid pricing model
     */
    UNDEFINED,
    /**
     * Free
     */
    FREE,
    /**
     * Fixed payment model with or without updates
     */
    FIXED,
    /**
     * Buy once, pay per row with optional reverse block rate
     */
    FIXED_PER_ROWS,
    /**
     * Buy once, pay based on population selection with optional reverse block rate
     */
    FIXED_FOR_POPULATION,
    /**
     * Pay per call, with optional prepaid SKUs and/or reverse block rate
     * pricing
     */
    PER_CALL(true),
    /**
     * Pay per row, with optional prepaid SKUs and/or reverse block rate pricing
     */
    PER_ROW(true),

    // Sentinel Hub

    /**
     * Sentinel Hub open data collections {@link https://www.sentinel-hub.com/}
     */
    SENTINEL_HUB_SUBSCRIPTION,
    /**
     * Sentinel Hub commercial data {@link https://www.sentinel-hub.com/}
     */
    SENTINEL_HUB_IMAGES,
    ;

    private EnumPricingModel() {
        this.useStatsSupported = false;
    }

    private EnumPricingModel(boolean useStatsSupported) {
        this.useStatsSupported = useStatsSupported;
    }

    @Getter
    public final boolean useStatsSupported;

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
