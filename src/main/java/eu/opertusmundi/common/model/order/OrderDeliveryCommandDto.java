package eu.opertusmundi.common.model.order;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDeliveryCommandDto {

    @JsonIgnore
    private UUID consumerKey;

    @JsonIgnore
    private UUID orderKey;

}
