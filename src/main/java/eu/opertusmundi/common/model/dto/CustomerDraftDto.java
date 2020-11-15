package eu.opertusmundi.common.model.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.EnumCustomerRegistrationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CustomerDraftDto {

    @JsonIgnore
    protected UUID bankAccountIdempotentKey;

    protected ZonedDateTime createdAt;

    protected String email;

    @JsonIgnore
    protected Integer id;

    @JsonIgnore
    protected UUID key;

    protected ZonedDateTime modifiedAt;

    protected EnumCustomerRegistrationStatus status;

    protected EnumCustomerType type;

    @JsonIgnore
    protected UUID userIdempotentKey;

    @JsonIgnore
    protected UUID walletIdempotentKey;

}
