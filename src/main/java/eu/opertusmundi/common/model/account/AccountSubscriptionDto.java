package eu.opertusmundi.common.model.account;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
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

    @Schema(description = "First asset topic category if any exist")
    private EnumTopicCategory segment;

    @JsonInclude(Include.NON_NULL)
    private CustomerDto consumer;

    @JsonInclude(Include.NON_NULL)
    private CustomerDto provider;

}
