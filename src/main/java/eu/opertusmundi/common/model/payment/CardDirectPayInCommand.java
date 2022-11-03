package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Command for creating a new card direct PayIn
 */
@Getter
@ToString(callSuper = true)
@JsonIgnoreType
public class CardDirectPayInCommand extends PayInCommand {

    @Builder
    public CardDirectPayInCommand(
        UUID key,
        UUID userKey,
        String cardId,
        BrowserInfoDto browserInfo,
        String ipAddress,
        PayInAddressCommandDto billing,
        PayInAddressCommandDto shipping
    ) {
        super(userKey, key);

        this.billing     = billing;
        this.browserInfo = browserInfo;
        this.cardId      = cardId;
        this.ipAddress   = ipAddress;
        this.shipping    = shipping;
    }

    private final PayInAddressCommandDto billing;
    private final BrowserInfoDto         browserInfo;
    private final String                 cardId;
    private final String                 ipAddress;
    private final PayInAddressCommandDto shipping;

}
