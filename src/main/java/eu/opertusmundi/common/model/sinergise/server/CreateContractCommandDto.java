package eu.opertusmundi.common.model.sinergise.server;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class CreateContractCommandDto {

    private String userEmail;

    private long accountTypeId;

    private ZonedDateTime validTo;

    private String givenName;

    private String familyName;

}
