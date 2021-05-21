package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SubscriptionBillingDto {

    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private Integer subscriptionId;

    @Schema(description = "Service unique PID")
    private String service;

    @Schema(description = "Service description")
    private String subscriptionDescription;

    @Schema(description = "Billing interval first date")
    private ZonedDateTime fromDate;

    @Schema(description = "Billing interval last date")
    private ZonedDateTime toDate;

    @Schema(description = "Total rows charged in this record. This field is exclusive with field `totalCalls`")
    private Integer totalRows;

    @Schema(description = "Total calls charged in this record. This field is exclusive with field `totalRows`")
    private Integer totalCalls;

    @Schema(description = "Total rows used by purchased SKUs. This field is exclusive with field `skuTotalCalls`")
    private Integer skuTotalRows;

    @Schema(description = "Total calls used by purchased SKUs. This field is exclusive with field `skuTotalRows`")
    private Integer skuTotalCalls;

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

}
