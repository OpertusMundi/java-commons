package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.account.helpdesk.SimpleHelpdeskAccountDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServiceBillingBatchDto {

    @JsonIgnore
    private Integer id;

    private UUID key;

    private SimpleHelpdeskAccountDto createdBy;

    private ZonedDateTime createdOn;

    private ZonedDateTime updatedOn;

    private EnumServiceBillingBatchStatus status;

    @Schema(description = "Billing interval first date")
    private LocalDate fromDate;

    @Schema(description = "Billing interval last date")
    private LocalDate toDate;

    @Schema(description = "Payment due date")
    private LocalDate dueDate;

    @Schema(description = "Total number of subscriptions")
    private Integer totalSubscriptions;

    @Schema(description = "Item total price ", example = "1.24")
    private BigDecimal totalPrice;

    @Schema(description = "Item price excluding tax", example = "1.00")
    private BigDecimal totalPriceExcludingTax;

    @Schema(description = "Item tax ", example = "0.24")
    private BigDecimal totalTax;

    private String processDefinition;

    private String processInstance;

}
