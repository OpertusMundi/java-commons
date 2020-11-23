package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.pricing.FixedPricingModelDto;
import eu.opertusmundi.common.model.pricing.FreePricingModelDto;
import eu.opertusmundi.common.model.pricing.SubscriptionPricingModelDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    oneOf = {
        FreePricingModelDto.class,
        FixedPricingModelDto.class,
        SubscriptionPricingModelDto.class
    }
)
public class PricingModelAsJson {

}
