package eu.opertusmundi.common.model.account;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CustomerDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected UUID contract;

    protected ZonedDateTime createdAt;

    protected String email;

    protected boolean emailVerified;

    protected ZonedDateTime emailVerifiedAt;

    @JsonIgnore
    protected Integer id;

    protected EnumKycLevel kycLevel;

    protected ZonedDateTime modifiedAt;

    @JsonIgnore
    protected String paymentProviderUser;

    @JsonIgnore
    protected String paymentProviderWallet;

    @JsonIgnore
    protected boolean termsAccepted;

    @JsonIgnore
    protected ZonedDateTime termsAcceptedAt;

    protected EnumMangopayUserType type;

    protected BigDecimal walletFunds;

    protected ZonedDateTime walletFundsUpdatedOn;

}
