package eu.opertusmundi.common.model.order;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderShippingCommandDto {

    @JsonIgnore
    private UUID publisherKey;

    @JsonIgnore
    private UUID orderKey;

    private String trackerUrl;

    private String referenceUrl;

}
