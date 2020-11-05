package eu.opertusmundi.common.model.dto;

import java.time.ZonedDateTime;

import eu.opertusmundi.common.model.EnumEventLevel;

/**
 * Query for searching system events
 */
public class EventQuery {

    private String userName;

    private String source;

    private EnumEventLevel level;

    private ZonedDateTime minDate;

    private ZonedDateTime maxDate;

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public EnumEventLevel getLevel() {
        return this.level;
    }

    public void setLevel(EnumEventLevel level) {
        this.level = level;
    }

    public ZonedDateTime getMinDate() {
        return this.minDate;
    }

    public void setMinDate(ZonedDateTime minDate) {
        this.minDate = minDate;
    }

    public ZonedDateTime getMaxDate() {
        return this.maxDate;
    }

    public void setMaxDate(ZonedDateTime maxDate) {
        this.maxDate = maxDate;
    }

}
