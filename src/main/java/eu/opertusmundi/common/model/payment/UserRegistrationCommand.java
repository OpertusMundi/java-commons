package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import eu.opertusmundi.common.model.dto.EnumCustomerType;
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

    private EnumCustomerType type;

    private UUID userKey;

    private UUID registrationKey;

    public static UserRegistrationCommand of(EnumCustomerType type, UUID userKey, UUID registrationKey) {
        final UserRegistrationCommand c = new UserRegistrationCommand();
        c.setRegistrationKey(registrationKey);
        c.setType(type);
        c.setUserKey(userKey);
        return c;
    }

}
