package eu.opertusmundi.common.model.logging;

import java.time.ZonedDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventDto {

    private long id;

    private String application;

    private ZonedDateTime createdOn;

    private EnumEventLevel level;

    private String message;

    private String exception;

    private String logger;

    private String clientAddress;

    private String userName;

}