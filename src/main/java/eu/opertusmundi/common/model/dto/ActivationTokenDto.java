package eu.opertusmundi.common.model.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import eu.opertusmundi.common.model.EnumActivationTokenType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ActivationTokenDto {

    private Integer                 account;
    private ZonedDateTime           createdAt = ZonedDateTime.now();
    private int                     duration;
    private String                  email;
    boolean                         expired;
    private Integer                 id;
    private ZonedDateTime           redeemedAt;
    private UUID                    token;
    private EnumActivationTokenType type;
    private boolean                 valid;

}
