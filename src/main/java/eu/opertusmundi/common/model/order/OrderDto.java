package eu.opertusmundi.common.model.order;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.catalogue.client.EnumDeliveryMethod;
import eu.opertusmundi.common.model.payment.EnumPaymentMethod;
import eu.opertusmundi.common.model.payment.PayInDto;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Hidden;
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
    @JsonInclude(Include.NON_EMPTY)
    private List<OrderItemDto> items = new ArrayList<>();

    @Hidden
    @ArraySchema(arraySchema = @Schema(
        description = "Order status history records. This property is available only to Helpdesk application"),
        minItems = 0,
        uniqueItems = true,
        schema = @Schema(implementation = OrderStatusDto.class)
    )
    @JsonInclude(Include.NON_EMPTY)
    private List<OrderStatusDto> statusHistory = new ArrayList<>();

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

    @Schema(hidden = true, description = "Order consumer customer. This property is available only to Helpdesk application")
    @JsonInclude(Include.NON_NULL)
    private CustomerDto consumer;

    @Schema(hidden = true, description = "Order linked PayIn object. This property is available only to Helpdesk application")
    @JsonInclude(Include.NON_NULL)
    private PayInDto payIn;

    public void addItem(OrderItemDto i) {
        this.items.add(i);
    }

    public void addStatusHistory(OrderStatusDto h) {
        this.statusHistory.add(h);
    }

}
