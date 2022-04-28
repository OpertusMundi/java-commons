package eu.opertusmundi.common.model.account;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.Message;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CustomerDraftDto {

    @JsonIgnore
    protected UUID bankAccountIdempotentKey;

    protected ZonedDateTime createdAt;

    protected String email;

    protected List<Message> errorDetails;

    @JsonIgnore
    protected Integer id;

    @JsonIgnore
    protected UUID key;

    protected ZonedDateTime modifiedAt;

    protected EnumCustomerRegistrationStatus status;

    protected EnumMangopayUserType type;

    @JsonIgnore
    protected UUID userIdempotentKey;

    @JsonIgnore
    protected UUID walletIdempotentKey;

}
