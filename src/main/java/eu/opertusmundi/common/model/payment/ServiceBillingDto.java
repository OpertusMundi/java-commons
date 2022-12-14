package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.account.EnumPayoffStatus;
import eu.opertusmundi.common.model.asset.service.UserServiceDto;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public abstract class ServiceBillingDto {

    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private Integer subscriptionId;

    @JsonIgnore
    private Integer userServiceId;

    private UUID key;

    private EnumBillableServiceType type;

    private ZonedDateTime createdOn;

    private ZonedDateTime updatedOn;

    @Schema(description = "Billing interval first date")
    private LocalDate fromDate;

    @Schema(description = "Billing interval last date")
    private LocalDate toDate;

    @Schema(description = "Payment due date")
    private LocalDate dueDate;

    @Schema(description = "Total rows charged in this record. This field is exclusive with field `totalCalls`")
    private Integer totalRows;

    @Schema(description = "Total calls charged in this record. This field is exclusive with field `totalRows`")
    private Integer totalCalls;

    @Schema(description =
        "Total rows used by purchased SKUs. This field is exclusive with field `skuTotalCalls`. "
      + "This field is applicable only for records of type `SUBSCRIPTION`"
    )
    private Integer skuTotalRows;

    @Schema(description =
        "Total calls used by purchased SKUs. This field is exclusive with field `skuTotalRows`. "
      + "This field is applicable only for records of type `SUBSCRIPTION`"
    )
    private Integer skuTotalCalls;

    @Schema(description = "Item total price ", example = "1.24")
    private BigDecimal totalPrice;

    @Schema(description = "Item price excluding tax", example = "1.00")
    private BigDecimal totalPriceExcludingTax;

    @Schema(description = "Item tax ", example = "0.24")
    private BigDecimal totalTax;

    @Schema(description = "Quotation pricing model")
    private BasePricingModelCommandDto pricingModel;

    @Schema(description = "Use statistics")
    private ServiceUseStatsDto stats;

    private EnumPayoffStatus status;

    @JsonInclude(Include.NON_NULL)
    private PayInDto payIn;

    @Schema(description = "Subscription or user service key")
    @JsonInclude(Include.NON_NULL)
    public UUID serviceKey;

    @Schema(description = "The provider of the subscription or the owner of the user service")
    @JsonInclude(Include.NON_NULL)
    private UUID providerKey;

    @Schema(description = "Owner's parent account key")
    @JsonInclude(Include.NON_NULL)
    private UUID providerParentKey;

    @Schema(description = "Consumer account key. This field is applicable only for records of type `SUBSCRIPTION`")
    @JsonInclude(Include.NON_NULL)
    private UUID consumerKey;
    
    @JsonInclude(Include.NON_NULL)
    private UserServiceDto userService;

}
