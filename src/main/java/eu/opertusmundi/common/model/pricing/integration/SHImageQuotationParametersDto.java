package eu.opertusmundi.common.model.pricing.integration;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class SHImageQuotationParametersDto extends QuotationParametersDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected SHImageQuotationParametersDto() {
        super(EnumPricingModel.SENTINEL_HUB_IMAGES);
    }

    @Schema(description = "Query used for selecting images", required = true)
    @Getter
    @Setter
    @NotNull
    private JsonNode query;

}
