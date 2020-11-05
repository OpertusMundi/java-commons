package eu.opertusmundi.common.model.dto;

import java.time.ZonedDateTime;

import eu.opertusmundi.common.model.EnumEventLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventDto {

    private String application;

    private ZonedDateTime createdOn;

    private EnumEventLevel level;

    private String message;

    private String exception;

    private String logger;

    private String clientAddress;

    private String userName;

}