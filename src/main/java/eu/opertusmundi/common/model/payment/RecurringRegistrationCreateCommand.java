package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Command for creating a recurring PayIn registration
 *
 * @see https://docs.mangopay.com/endpoints/v2.01/payins#e1051_create-a-recurring-payin-registration
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreType
public class RecurringRegistrationCreateCommand {

    /**
     * Unique key used for retrying requests
     */
    private String idempotencyKey;

    /**
     * The consumer account unique key
     */
    private UUID userKey;

    /**
     * The order for which this registration is created
     */
    private UUID orderKey;

    /**
     * The MANGOPAY identifier of the user who is creating the registration. The
     * value is set from the consumer account specified by @{link
     * {@link #userKey}.
     */
    private String authorId;

    /**
     * Provider specific recurring payment registration identifier
     */
    private String registrationId;

    /**
     * The MANGOPAY identifier of the card. The card must registered to the
     * account referred by {@link #userKey}
     */
    private String cardId;

    /**
     * The MANGOPAY identifier of the user who is credited. The value is always
     * equal to {@link #authorId}
     */
    public String getCreditedUserId() {
        return this.authorId;
    }

    /**
     * The MANGOPAY identifier of the wallet where money will be credited. The
     * value is set from the consumer account specified by @{link
     * {@link #userKey}
     */
    private String creditedWalletId;

    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private ZonedDateTime createdOn = ZonedDateTime.now();

    private EnumRecurringPaymentStatus status;

    /**
     * Amount of the first payment
     */
    private BigDecimal firstTransactionDebitedFunds;

    /**
     * User billing address information
     */
    private PayInAddressCommandDto billingAddress;

    /**
     * User shipping address information
     */
    private PayInAddressCommandDto shippingAddress;

    /**
     * Date on which the recurring payments will end
     */
    private ZonedDateTime endDate;

    /**
     * Frequency at which the recurring payments will be made
     */
    private EnumRecurringPaymentFrequency frequency;

    /**
     * True if this is a migration of an existing recurring PayIn registration
     */
    @Builder.Default
    private boolean migrate = false;

    /**
     * The MANGOPAY identifier of the migrated registration
     */
    private String migrateRegistration;

}
