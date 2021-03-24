package eu.opertusmundi.common.model.order;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.catalogue.client.EnumDeliveryMethod;
import eu.opertusmundi.common.model.payment.EnumPaymentMethod;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDto {

    @JsonIgnore
    private Integer id;

    @Schema(
        description = "Order unique key",
        example = "53dd19d0-7498-40bc-8632-7ab125c73808"
    )
    private UUID key;

    @ArraySchema(arraySchema = @Schema(
        description = "Order items. Currently only a single item is allowed per order"), 
        minItems = 1, 
        maxItems = 1,
        uniqueItems = true, 
        schema = @Schema(implementation = OrderItemDto.class)
    )
    private List<OrderItemDto> items = new ArrayList<>();
    
    @JsonIgnore
    private Integer cartId;

    @Schema(description = "The total price of all PayIn items (the debited funds of the PayIn)")
    protected BigDecimal totalPrice;

    @Schema(description = "The total price of all PayIn items excluding tax")
    protected BigDecimal totalPriceExcludingTax;

    @Schema(description = "The total tax for all PayIn items")
    protected BigDecimal totalTax;

    @Schema(
        description = "The currency in ISO 4217 format. Only `EUR` is supported", 
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_4217")
    )
    protected String currency;

    private ZonedDateTime createdOn;

    private EnumOrderStatus status;

    private ZonedDateTime statusUpdatedOn;

    private EnumDeliveryMethod deliveryMethod;

    private EnumPaymentMethod paymentMethod;

    @Schema(description = "User friendly reference code for support")
    private String referenceNumber;
    
    public void addItem(OrderItemDto i) {
        this.items.add(i);
    }
   
}
