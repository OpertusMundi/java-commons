package eu.opertusmundi.common.model.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonIgnoreType
@AllArgsConstructor
@Builder
public class RecurringRegistrationUpdateStatusCommand {

    private final String registrationId;

    private final EnumRecurringPaymentStatus status;

}
