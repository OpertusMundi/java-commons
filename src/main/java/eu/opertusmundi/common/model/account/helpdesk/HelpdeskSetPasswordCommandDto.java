package eu.opertusmundi.common.model.account.helpdesk;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class HelpdeskSetPasswordCommandDto {

    private String password;
    private String passwordMatch;

}
