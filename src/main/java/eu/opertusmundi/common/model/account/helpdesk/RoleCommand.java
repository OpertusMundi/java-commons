package eu.opertusmundi.common.model.account.helpdesk;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName = "of")
@Getter
public class RoleCommand {

    int              accountId;
    Integer          grantedBy;
    EnumHelpdeskRole role;
}
