package eu.opertusmundi.common.model.payment;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Command for creating a new card direct PayIn
 */
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreType
public class CardDirectPayInCommand extends PayInCommand {

    @Builder
    public CardDirectPayInCommand(
        UUID userKey, UUID orderKey, Integer debitedFunds, String referenceNumber, String payIn,
        ZonedDateTime createdOn, EnumTransactionStatus status, ZonedDateTime executedOn, String resultCode, String resultMessage,
        String cardId, String statementDescriptor, BrowserInfoDto browserInfo,
        String ipAddress, PayInAddressCommandDto billing, PayInAddressCommandDto shipping,
        String requested3dsVersion, String applied3dsVersion
    ) {
        super(userKey, orderKey, debitedFunds, referenceNumber, payIn, createdOn, status, executedOn, resultCode, resultMessage);

        this.applied3dsVersion   = applied3dsVersion;
        this.billing             = billing;
        this.browserInfo         = browserInfo;
        this.cardId              = cardId;
        this.ipAddress           = ipAddress;
        this.requested3dsVersion = requested3dsVersion;
        this.shipping            = shipping;
        this.statementDescriptor = statementDescriptor;
    }

    private String applied3dsVersion;

    private PayInAddressCommandDto billing;

    private BrowserInfoDto browserInfo;

    private String cardId;

    private String ipAddress;

    private String requested3dsVersion;

    private PayInAddressCommandDto shipping;

    private String statementDescriptor;

}
