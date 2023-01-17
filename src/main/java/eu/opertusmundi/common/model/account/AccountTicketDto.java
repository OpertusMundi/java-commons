package eu.opertusmundi.common.model.account;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.account.helpdesk.SimpleHelpdeskAccountDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountTicketDto {

    @JsonIgnore
    private Integer id;

    private UUID             key;
    private EnumTicketType   type;
    private UUID             resourceKey;
    private EnumTicketStatus status;
    private String           subject;
    private String           message;
    private UUID             messageThreadKey;
    private ZonedDateTime    createdAt;
    private ZonedDateTime    updatedAt;

    @JsonInclude(Include.NON_NULL)
    private SimpleAccountDto owner;

    @JsonInclude(Include.NON_NULL)
    private ZonedDateTime assignedAt;

    @JsonInclude(Include.NON_NULL)
    private SimpleHelpdeskAccountDto assignee;

}
