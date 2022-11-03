package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Command for creating a new Bank wire PayIn
 */
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@JsonIgnoreType
public class BankwirePayInCommand extends PayInCommand {

    @Builder
    public BankwirePayInCommand(UUID userKey, UUID orderKey) {
        super(userKey, orderKey);
    }

}
