package eu.opertusmundi.common.model.account.helpdesk;

import java.io.Serializable;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SimpleHelpdeskAccountDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private UUID    key;
    private String  email;
    private String  firstName;
    private String  lastName;

}
