package eu.opertusmundi.common.model.account;

import java.io.Serializable;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimpleAccountDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID   key;
    private String username;

}
