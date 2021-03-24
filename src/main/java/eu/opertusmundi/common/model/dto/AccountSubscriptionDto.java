package eu.opertusmundi.common.model.dto;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountSubscriptionDto {

    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private Integer orderId;

    @Schema(description = "Service PID")
    private String service;

    @Schema(description = "When the subscription was registered to the user account")
    private ZonedDateTime addedOn;

    @Schema(description = "Date of last update")
    private ZonedDateTime updatedOn;

    @Schema(description = "Operation that registered the subscription")
    private EnumAssetSource source;

}
