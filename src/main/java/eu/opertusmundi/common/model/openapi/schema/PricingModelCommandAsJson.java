package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.pricing.FixedPopulationPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.FixedPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.FixedRowPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.FreePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.PerCallPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.PerRowPricingModelCommandDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    oneOf = {
        FreePricingModelCommandDto.class,
        FixedPricingModelCommandDto.class,
        FixedRowPricingModelCommandDto.class,
        FixedPopulationPricingModelCommandDto.class,
        PerCallPricingModelCommandDto.class,
        PerRowPricingModelCommandDto.class,
    }
)
public class PricingModelCommandAsJson {

}
