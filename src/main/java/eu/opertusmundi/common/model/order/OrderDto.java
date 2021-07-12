package eu.opertusmundi.common.model.order;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.catalogue.client.EnumDeliveryMethod;
import eu.opertusmundi.common.model.payment.EnumPaymentMethod;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class OrderDto {

    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private Integer cartId;

    @Schema(
        description = "Order unique key",
        example = "53dd19d0-7498-40bc-8632-7ab125c73808"
    )
    private UUID key;

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

    @Schema(description = "Date of creation in ISO format", example = "2021-06-18T10:48:19.684+03:00")
    private ZonedDateTime createdOn;

    @Schema(description = "Current order status")
    private EnumOrderStatus status;

    @Schema(description = "Date of last status update in ISO format", example = "2021-06-18T10:48:19.684+03:00")
    private ZonedDateTime statusUpdatedOn;

    @Schema(description = "Delivery method")
    private EnumDeliveryMethod deliveryMethod;

    @Schema(description = "Payment method")
    private EnumPaymentMethod paymentMethod;

    @Schema(description = "User friendly reference code for support")
    private String referenceNumber;

}
