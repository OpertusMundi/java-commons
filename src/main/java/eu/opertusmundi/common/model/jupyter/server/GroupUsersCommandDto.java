package eu.opertusmundi.common.model.jupyter.server;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupUsersCommandDto {

    private List<String> users;

}
