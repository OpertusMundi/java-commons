package eu.opertusmundi.common.model.logging;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

public class ElasticEventDto {

    private String application;

    @JsonProperty("program-name")
    public void setApplication(String value) {
        this.application = value;
    }

    @JsonProperty("application")
    public String getApplication() {
        return this.application;
    }

    private String clientAddress;

    @JsonProperty("fromhost")
    public void setClientAddress(String value) {
        this.clientAddress = value;
    }

    @JsonProperty("clientAddress")
    public String getClientAddress() {
        return this.clientAddress;
    }

    @Getter
    @Setter
    private String exception;

    private String exceptionMessage;

    @JsonProperty("exception-message")
    public void setExceptionMessage(String value) {
        this.exceptionMessage = value;
    }

    @JsonProperty("exceptionMessage")
    public String getExceptionMessage() {
        return this.exceptionMessage;
    }

    @Getter
    @Setter
    private String facility;

    @Getter
    @Setter
    private String hostname;

    private EnumEventLevel level;

    @JsonProperty("severity")
    private void setLevel(String value) {
        this.level = EnumEventLevel.fromSeverity(value);
    }

    @JsonProperty("level")
    private EnumEventLevel getLevel() {
        return this.level;
    }

    @Getter
    @Setter
    private String logger;

    @Getter
    @Setter
    private String message;

    private Long procId;

    @JsonProperty("procid")
    public void setProcId(Long value) {
        this.procId = value;
    }

    @JsonProperty("procId")
    public Long getProcId() {
        return this.procId;
    }

    private Long severityNumber;

    @JsonProperty("severity-number")
    public void setSeverityNumber(Long value) {
        this.severityNumber = value;
    }

    @JsonProperty("severityNumber")
    public Long getSeverityNumber() {
        return this.severityNumber;
    }

    @Getter
    @Setter
    private String tag;

    @Getter
    @Setter
    private String thread;

    @Getter
    @Setter
    private ZonedDateTime timestamp;

}