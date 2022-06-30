package eu.opertusmundi.common.model.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SubscriptionBillingBatchCommandDto {

    @JsonIgnore
    private Integer userId;

    private int year;

    private int month;

    @Schema(description = "When `true`, subscription billing records are computed but neither the database is updated nor notifications are sent")
    private boolean quotationOnly;
}
