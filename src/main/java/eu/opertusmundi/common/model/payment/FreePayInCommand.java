package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Command for creating a new PayIn with zero debited funds
 */
@NoArgsConstructor
@ToString(callSuper = true)
@JsonIgnoreType
public class FreePayInCommand extends PayInCommand {

    @Builder
    public FreePayInCommand(UUID userKey, UUID orderKey) {
        super(userKey, orderKey);
    }

}
