package eu.opertusmundi.common.model.sinergise.server;

import java.time.ZonedDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractDto {

    private int id;

    private String name;

    private long accountTypeId;

    private ZonedDateTime validTo;

    private ZonedDateTime lastUpdatedOn;

}
