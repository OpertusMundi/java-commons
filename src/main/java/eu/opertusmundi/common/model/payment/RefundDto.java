package eu.opertusmundi.common.model.payment;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundDto {

    @Hidden
    @JsonInclude(Include.NON_EMPTY)
    private String refund;

    @JsonInclude(Include.NON_NULL)
    private ZonedDateTime refundCreatedOn;

    @JsonInclude(Include.NON_NULL)
    private ZonedDateTime refundExecutedOn;

    @JsonInclude(Include.NON_NULL)
    private EnumTransactionStatus refundStatus;

    @JsonInclude(Include.NON_EMPTY)
    private String refundReasonType;

    @JsonInclude(Include.NON_EMPTY)
    private String refundReasonMessage;

}
