package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@JsonIgnoreType
public class UserCardCommand extends UserCommand {

    private String cardId;

    @Builder
    public UserCardCommand(UUID userKey, String cardId) {
        super(userKey);

        this.cardId = cardId;
    }

    public static UserCardCommand of(UUID userKey, String cardId) {
        final UserCardCommand c = new UserCardCommand();
        c.setCardId(cardId);
        c.setUserKey(userKey);
        return c;
    }

}
