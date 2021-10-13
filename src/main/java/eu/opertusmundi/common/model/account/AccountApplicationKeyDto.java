package eu.opertusmundi.common.model.account;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountApplicationKeyDto {

    @JsonIgnore
    private Integer id;

    private ZonedDateTime createdOn;

    private ZonedDateTime revokedOn;

    private String key;

}
