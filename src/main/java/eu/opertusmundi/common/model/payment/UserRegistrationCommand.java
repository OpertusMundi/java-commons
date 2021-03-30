package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreType
public class UserRegistrationCommand {

    private UUID userKey;

    private UUID registrationKey;

    public static UserRegistrationCommand of(UUID userKey, UUID registrationKey) {
        final UserRegistrationCommand c = new UserRegistrationCommand();
        c.setUserKey(userKey);
        c.setRegistrationKey(registrationKey);
        return c;
    }

}
