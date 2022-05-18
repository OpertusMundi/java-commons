package eu.opertusmundi.common.model.account;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CustomerDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected UUID key;

    protected UUID contract;

    protected ZonedDateTime createdAt;

    protected String email;

    protected boolean emailVerified;

    protected ZonedDateTime emailVerifiedAt;

    @JsonIgnore
    protected Integer id;

    protected EnumKycLevel kycLevel;

    protected ZonedDateTime modifiedAt;

    @JsonInclude(Include.NON_EMPTY)
    protected String paymentProviderUser;

    @JsonInclude(Include.NON_EMPTY)
    protected String paymentProviderWallet;

    @JsonIgnore
    protected boolean termsAccepted;

    @JsonIgnore
    protected ZonedDateTime termsAcceptedAt;

    protected EnumMangopayUserType type;

    @Schema(description = "Wallet funds. Wallet data is updated when a new PayIn, Transfer or PayOut is created.")
    protected BigDecimal walletFunds;

    @Schema(description = "Wallet last update. Wallet data is updated when a new PayIn, Transfer or PayOut is created.")
    protected ZonedDateTime walletFundsUpdatedOn;

    @Schema(description = "Indicates if a user has her inflows (payins, incoming transfers) blocked")
    protected boolean blockedInflows;

    @Schema(description = "Indicates if a user has her outflows (payouts, outgoing transfers) blocked.")
    protected boolean blockedOutflows;

}
