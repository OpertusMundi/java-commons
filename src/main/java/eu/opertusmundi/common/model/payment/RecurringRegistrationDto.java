package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecurringRegistrationDto {

    @JsonIgnore
    private Integer id;

    private UUID key;

    private AccountSubscriptionDto subscription;

    private List<PayInDto> payins = new ArrayList<>();

    private List<RecurringRegistrationStatusDto> statusHistory = new ArrayList<>();

    private String providerCard;

    @JsonIgnore
    private String providerRegistration;

    private BigDecimal firstTransactionDebitedFunds;

    private BigDecimal nextTransactionDebitedFunds;

    private String currency;

    private ZonedDateTime endDate;

    private EnumRecurringPaymentFrequency frequency;

    private boolean fixedNextAmount;

    private boolean fractionedPayment;

    @JsonIgnore
    private boolean migration;

    private PayInAddressDto billingAddress;

    private PayInAddressDto shippingAddress;

    private ZonedDateTime createdOn;

    private EnumRecurringPaymentStatus status;

    private ZonedDateTime statusUpdatedOn;

}
