package eu.opertusmundi.common.model.payment;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@Setter
@JsonIgnoreType
public class PayInStatusUpdateCommand {

    private String providerPayInId;

    private ZonedDateTime executedOn;

    private EnumTransactionStatus status;

    private String resultCode;

    private String resultMessage;

}
