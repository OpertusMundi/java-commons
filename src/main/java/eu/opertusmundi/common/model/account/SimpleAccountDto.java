package eu.opertusmundi.common.model.account;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimpleAccountDto {

    private UUID   key;
    private String username;

}
