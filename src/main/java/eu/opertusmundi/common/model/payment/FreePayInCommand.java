package eu.opertusmundi.common.model.payment;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Command for creating a new Bank wire PayIn
 */
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreType
public class FreePayInCommand extends PayInCommand {

    @Builder
    public FreePayInCommand(UUID userKey, UUID orderKey, String referenceNumber, ZonedDateTime createdOn) {
        super(userKey, orderKey, 0, referenceNumber, null, createdOn, EnumTransactionStatus.SUCCEEDED, createdOn, null, null);
    }

}
