package eu.opertusmundi.common.model.order;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDto {

    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private Integer orderId;

    @Schema(
        description = "Index of the specific item in the order",
        example = "5"
    )
    private Integer index;

    @Schema(description = "Item type")
    private EnumOrderItemType type;

    @Schema(
        description = "Catalogue item unique Id",
        example = "opertusmundi.topio.1.asset"
    )
    private String item;

    @Schema(description = "Item description at the time of the purchase")
    private String description;

    @Schema(description = "Pricing model at the time of the purchase")
    private EffectivePricingModelDto pricingModel;

    @Schema(
        description = "Item total price ",
        example = "1,24"
    )
    private BigDecimal totalPrice;

    @Schema(
        description = "Item price excluding tax",
        example = "1,00"
    )
    private BigDecimal totalPriceExcludingTax;

    @Schema(
        description = "Item tax ",
        example = "0,24"
    )
    private BigDecimal totalTax;

    @Schema(description = "Optional discount code applied to the item's price")
    private String discountCode;

    @JsonInclude(Include.NON_NULL)
    private CustomerDto provider;

}
