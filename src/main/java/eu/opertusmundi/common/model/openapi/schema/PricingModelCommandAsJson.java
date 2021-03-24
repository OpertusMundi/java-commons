package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.pricing.CallBlockRatePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.CallPrePaidPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.FixedPopulationPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.FixedPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.FixedRowPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.FreePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.RowBlockRatePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.RowPrePaidPricingModelCommandDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    oneOf = {
        FreePricingModelCommandDto.class,
        FixedPricingModelCommandDto.class,
        FixedRowPricingModelCommandDto.class,
        FixedPopulationPricingModelCommandDto.class,
        CallPrePaidPricingModelCommandDto.class,
        CallBlockRatePricingModelCommandDto.class,
        RowPrePaidPricingModelCommandDto.class,
        RowBlockRatePricingModelCommandDto.class,
    }
)
public class PricingModelCommandAsJson {

}
