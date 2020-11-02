package eu.opertusmundi.common.model.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimplAccountDto {

    private UUID   key;
    private String username;

}
