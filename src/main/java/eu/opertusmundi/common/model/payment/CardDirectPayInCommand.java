package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Command for creating a new card direct PayIn
 */
@JsonIgnoreType
@ToString
public class CardDirectPayInCommand extends PayInCommand {

    @Builder
    public CardDirectPayInCommand(
        UUID userKey, UUID orderKey,
        String creditedUserId, String creditedWalletId,
        BigDecimal debitedFunds, String referenceNumber, String payIn,
        ZonedDateTime createdOn, EnumTransactionStatus status, ZonedDateTime executedOn, String resultCode, String resultMessage,
        String cardId, String cardAlias, String statementDescriptor, BrowserInfoDto browserInfo,
        String ipAddress, PayInAddressCommandDto billing, PayInAddressCommandDto shipping,
        String requested3dsVersion, String applied3dsVersion,
        boolean recurring, String recurringPayinRegistrationId, EnumRecurringPaymentType recurringTransactionType,
        EnumRecurringPaymentFrequency recurringPaymentfrequency
    ) {
        super(userKey, orderKey, debitedFunds, referenceNumber, payIn, createdOn, status, executedOn, resultCode, resultMessage);

        this.applied3dsVersion            = applied3dsVersion;
        this.billing                      = billing;
        this.browserInfo                  = browserInfo;
        this.cardAlias                    = cardAlias;
        this.cardId                       = cardId;
        this.creditedUserId               = creditedUserId;
        this.creditedWalletId             = creditedWalletId;
        this.ipAddress                    = ipAddress;
        this.recurring                    = recurring;
        this.recurringPaymentfrequency    = recurringPaymentfrequency;
        this.recurringPayinRegistrationId = recurringPayinRegistrationId;
        this.recurringTransactionType     = recurringTransactionType;
        this.requested3dsVersion          = requested3dsVersion;
        this.shipping                     = shipping;
        this.statementDescriptor          = statementDescriptor;
    }

    /**
     * The MANGOPAY identifier of the user who is creating the PayIn
     */
    @Getter
    @Setter
    private String creditedUserId;

    /**
     * The MANGOPAY identifier of the wallet where money will be credited
     */
    @Getter
    @Setter
    private String creditedWalletId;

    @Getter
    @Setter
    private String applied3dsVersion;

    @Getter
    private final PayInAddressCommandDto billing;

    @Getter
    private final BrowserInfoDto browserInfo;

    @Getter
    private final String cardId;

    @Getter
    @Setter
    private String cardAlias;

    @Getter
    private final String ipAddress;

    @Getter
    @Setter
    private String requested3dsVersion;

    @Getter
    private final PayInAddressCommandDto shipping;

    @Getter
    @Setter
    private String statementDescriptor;

    @Getter
    @Setter
    private String idempotencyKey;

    @Getter
    @Setter
    private boolean recurring = false;

    /**
     * The MANGOPAY recurring registration identifier
     */
    @Getter
    @Setter
    private String recurringPayinRegistrationId;

    @Getter
    @Setter
    private EnumRecurringPaymentType recurringTransactionType;

    /**
     * Frequency at which the recurring payments will be made
     */
    @Getter
    @Setter
    private EnumRecurringPaymentFrequency recurringPaymentfrequency;

}
