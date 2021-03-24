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
public class UserRegistrationCommandDto {

    private UUID userKey;

    private UUID registrationKey;

    public static UserRegistrationCommandDto of(UUID userKey, UUID registrationKey) {
        final UserRegistrationCommandDto c = new UserRegistrationCommandDto();
        c.setUserKey(userKey);
        c.setRegistrationKey(registrationKey);
        return c;
    }

}
