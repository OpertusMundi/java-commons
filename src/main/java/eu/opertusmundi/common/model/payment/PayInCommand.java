package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Base PayIn class
 *
 * Credited user and wallet are derived from the {@link PayInCommand#userKey}
 * property.
 */
@NoArgsConstructor
@Getter
@ToString
@JsonIgnoreType
public abstract class PayInCommand {

    public PayInCommand(UUID userKey, UUID key) {
        super();
        this.userKey  = userKey;
        this.key = key;
    }

    /**
     * User who requested the payment
     */
    protected UUID userKey;

    /**
     * The Order or PayIn key. For orders, the the PayIn key is equal to the
     * Order Key
     */
    protected UUID key;

}
