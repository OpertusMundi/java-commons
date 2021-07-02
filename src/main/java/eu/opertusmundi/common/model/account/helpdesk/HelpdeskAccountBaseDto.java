package eu.opertusmundi.common.model.account.helpdesk;

import java.io.Serializable;
import java.util.Set;

import javax.validation.constraints.NotEmpty;

import eu.opertusmundi.common.model.account.helpdesk.EnumHelpdeskRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public abstract class HelpdeskAccountBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean active;

    private boolean blocked;

    @NotEmpty
    private String email;

    @NotEmpty
    private String firstName;

    private byte[] image;

    private String imageMimeType;

    @NotEmpty
    private String lastName;

    @NotEmpty
    private String locale;

    @NotEmpty
    private String mobile;

    private String phone;

    private Set<EnumHelpdeskRole> roles;

    public boolean hasRole(EnumHelpdeskRole role) {
        return this.roles.contains(role);
    }

}
