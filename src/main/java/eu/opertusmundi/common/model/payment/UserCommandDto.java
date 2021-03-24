package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserCommandDto {

    private UUID userKey;

    public UserCommandDto(UUID userKey) {
        this.userKey = userKey;
    }

    public static UserCommandDto of(UUID userKey) {
        final UserCommandDto c = new UserCommandDto();
        c.setUserKey(userKey);
        return c;
    }

}
