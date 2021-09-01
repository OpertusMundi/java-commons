package eu.opertusmundi.common.model.jupyter.server;

import java.util.List;

@lombok.Getter
@lombok.Setter
public class GroupUsersCommandDto {

    /**
     * The list of names of affected users (added or removed)
     */
    private List<String> users;

}
