package eu.opertusmundi.common.model.account.helpdesk;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import eu.opertusmundi.common.model.account.helpdesk.HelpdeskAccountBaseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class HelpdeskAccountDto extends HelpdeskAccountBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private ZonedDateTime createdOn;
    private boolean       emailVerified;
    private ZonedDateTime emailVerifiedOn;
    private Integer       id;
    private UUID          key;
    private ZonedDateTime modifiedOn;
    private boolean       registeredToIdp;

}
