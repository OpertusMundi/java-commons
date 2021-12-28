package eu.opertusmundi.common.model.payment;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mangopay.entities.Event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventDto {

    @JsonIgnore
    private String id;

    private String resourceId;

    private String type;

    private String tag;

    private ZonedDateTime timestamp;

    public static EventDto from(Event e) {
        final EventDto o = new EventDto();

        o.setId(e.getId());
        o.setResourceId(e.getResourceId());
        o.setTag(e.getTag());
        o.setTimestamp(ZonedDateTime.ofInstant(Instant.ofEpochSecond(e.getDate()), ZoneOffset.UTC));
        o.setType(e.getEventType().toString());

        return o;
    }
}
