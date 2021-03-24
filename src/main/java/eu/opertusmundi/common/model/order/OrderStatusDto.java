package eu.opertusmundi.common.model.order;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.dto.SimplAccountDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusDto {

    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private Integer orderId;

    private EnumOrderStatus status;

    private ZonedDateTime statusUpdatedOn;
    
    private SimplAccountDto statusUpdatedBy;

}
