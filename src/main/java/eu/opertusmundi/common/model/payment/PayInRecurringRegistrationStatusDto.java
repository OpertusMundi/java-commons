package eu.opertusmundi.common.model.payment;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PayInRecurringRegistrationStatusDto {

    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private Integer registration;

    @Schema(description = "Registration status")
    private EnumRecurringPaymentStatus status;

    @Schema(description = "Date of update")
    private ZonedDateTime updatedOn;

}
