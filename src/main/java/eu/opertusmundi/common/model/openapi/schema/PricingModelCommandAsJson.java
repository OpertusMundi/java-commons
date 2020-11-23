package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.pricing.FixedPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.FreePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.SubscriptionPricingModelCommandDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    oneOf = {
        FreePricingModelCommandDto.class,
        FixedPricingModelCommandDto.class,
        SubscriptionPricingModelCommandDto.class
    }
)
public class PricingModelCommandAsJson {

}
