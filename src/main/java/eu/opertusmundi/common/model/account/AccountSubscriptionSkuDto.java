package eu.opertusmundi.common.model.account;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountSubscriptionSkuDto {

    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private Integer subscriptionId;
    
    @JsonIgnore
    private Integer orderId;

    @Schema(description = "Purchased rows. This field is exclusive with field `purchasedCalls` ")
    private Integer purchasedRows;

    @Schema(description = "Purchased calls. This field is exclusive with field `purchasedRows` ")
    private Integer purchasedCalls;

    @Schema(description = "Used rows. This field is updated at the end of each billing period")
    private Integer usedRows;

    @Schema(description = "Used calls. This field is updated at the end of each billing period")
    private Integer usedCalls;

}
