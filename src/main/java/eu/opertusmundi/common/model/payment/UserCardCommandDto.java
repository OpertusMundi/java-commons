package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class UserCardCommandDto extends UserCommandDto {

    private String cardId;

    @Builder
    public UserCardCommandDto(UUID userKey, String cardId) {
        super(userKey);

        this.cardId = cardId;
    }

    public static UserCardCommandDto of(UUID userKey, String cardId) {
        final UserCardCommandDto c = new UserCardCommandDto();
        c.setCardId(cardId);
        c.setUserKey(userKey);
        return c;
    }

}
