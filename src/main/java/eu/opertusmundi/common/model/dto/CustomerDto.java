package eu.opertusmundi.common.model.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CustomerDto {

    protected UUID contract;

    protected ZonedDateTime createdAt;

    protected String email;

    protected boolean emailVerified;

    protected ZonedDateTime emailVerifiedAt;

    @JsonIgnore
    protected Integer id;

    protected EnumKycLevel kycLevel;

    protected ZonedDateTime modifiedAt;

    protected String paymentProviderUser;

    protected String paymentProviderWallet;

    @JsonIgnore
    protected boolean termsAccepted;

    @JsonIgnore
    protected ZonedDateTime termsAcceptedAt;

    protected EnumCustomerType type;

}
