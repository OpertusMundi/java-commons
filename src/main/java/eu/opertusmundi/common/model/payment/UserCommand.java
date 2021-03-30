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
public class UserCommand {

    private UUID userKey;

    public UserCommand(UUID userKey) {
        this.userKey = userKey;
    }

    public static UserCommand of(UUID userKey) {
        final UserCommand c = new UserCommand();
        c.setUserKey(userKey);
        return c;
    }

}
