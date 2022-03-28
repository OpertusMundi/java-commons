package eu.opertusmundi.common.model.order;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class CartItemDto {

    @Schema(description = "Cart item unique identifier")
    @JsonProperty("id")
    @Getter
    @Setter
    private UUID key;

    @JsonIgnore
    @Getter
    @Setter
    private String assetId;

    @Schema(description = "Catalogue item. In contrast to catalogue responses, a cart item "
                        + "contains no pricing models. The effective pricing model is stored "
                        + "in property `pricingModel`")
    @JsonProperty("asset")
    @Getter
    @Setter
    private CatalogueItemDto asset;

    @Schema(description = "Date added to the cart")
    @Getter
    @Setter
    private ZonedDateTime addedAt;

    @Schema(description = "Selected pricing model")
    @Getter
    @Setter
    private EffectivePricingModelDto pricingModel;

    @JsonIgnore
    public String getPricingModelKey() {
        return pricingModel.getModel().getKey();
    }

    @JsonIgnore
    public QuotationParametersDto getQuotationParameters() {
        return pricingModel.getUserParameters();
    }

}
