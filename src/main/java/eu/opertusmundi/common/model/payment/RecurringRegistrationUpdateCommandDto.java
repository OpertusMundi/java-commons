package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

/**
 * Command for updating an existing recurring PayIn registration
 *
 * @see https://docs.mangopay.com/endpoints/v2.01/payins#e1057_update-a-recurring-payin-registration
 */
@Getter
@Setter
public class RecurringRegistrationUpdateCommandDto {

    /**
     * The consumer account unique identifier
     */
    @JsonIgnore
    private UUID userKey;

    /**
     * The MANGOPAY identifier of the card. The card must registered to the
     * account referred by {@link #userKey}
     */
    @Size(min = 1, max = 255)
    private String card;

    /**
     * User billing address information
     */
    @Valid
    private PayInAddressDto billingAddress;

    /**
     * User shipping address information
     */
    @Valid
    private PayInAddressDto shippingAddress;

}
