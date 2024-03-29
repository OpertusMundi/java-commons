package eu.opertusmundi.common.model.order;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class OrderItemDto {

    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private Integer orderId;

    @Schema(description = "Index of the specific item in the order", example = "5")
    private Integer index;

    @Schema(description = "Item type")
    private EnumOrderItemType type;

    @Schema(description = "Catalogue item unique PID", example = "opertusmundi.topio.1.asset")
    private String assetId;

    @Schema(description = "Item description at the time of the purchase")
    @JsonProperty("description")
    private String assetTitle;

    @Schema(description = "Catalogue item version", example = "1.1.0")
    private String assetVersion;

    @Schema(description = "Contract type")
    private EnumContractType contractType;

    @Schema(description = "Asset contract signature date")
    @JsonInclude(Include.NON_NULL)
    private ZonedDateTime contractSignedOn;

    @Schema(description = "Pricing model at the time of the purchase")
    private EffectivePricingModelDto pricingModel;

    @Schema(description = "Item total price ", example = "1.24")
    private BigDecimal totalPrice;

    @Schema(description = "Item price excluding tax", example = "1.00")
    private BigDecimal totalPriceExcludingTax;

    @Schema(description = "Item tax ", example = "0.24")
    private BigDecimal totalTax;

    @Schema(description = "Optional discount code applied to the item's price")
    private String discountCode;

}
