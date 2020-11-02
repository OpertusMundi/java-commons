package eu.opertusmundi.common.model.dto;

import java.time.ZonedDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountProfileConsumerDto extends AccountProfileConsumerBaseDto {

    private static final long serialVersionUID = 1L;

    private AddressDto billingAddress;

    private AddressDto shippingAddress;

    private ZonedDateTime registeredOn;

    private ZonedDateTime modifiedOn;

}
