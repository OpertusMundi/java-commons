package eu.opertusmundi.common.model.account.helpdesk;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EnumHelpdeskRole
{
    ADMIN       ("system administrator"),
    USER       	("User"),
    DEVELOPER  	("Developer role that enables additional experimental features"),
    ;

	private final String description;

}
