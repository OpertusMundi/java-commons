package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.EnumEventLevel;
import eu.opertusmundi.common.model.dto.EventDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Event")
@Table(schema = "logging", name = "log4j_message")
public class EventEntity {

    @Id
    @Column(name = "id", updatable = false)
    @SequenceGenerator(sequenceName = "logging.log4j_message_id_seq", name = "log4j_message_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "log4j_message_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private final long id = -1L;

    @NotNull
    @Column(name = "`application`", nullable = false)
    @Getter
    @Setter
    private String application;

    @Column(name = "generated")
    @Getter
    @Setter
    private ZonedDateTime generated;

    @NotNull
    @Column(name = "`level`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumEventLevel level;

    @Column(name = "`message`")
    @Getter
    @Setter
    private String message;

    @Column(name = "`throwable`")
    @Getter
    @Setter
    private String throwable;

    @Column(name = "`logger`")
    @Getter
    @Setter
    private String logger;

    @Column(name = "`client_address`")
    @Getter
    @Setter
    private String clientAddress;

    @Column(name = "`username`")
    @Getter
    @Setter
    private String userName;

    public EventDto toDto() {
        final EventDto e = new EventDto();

        e.setClientAddress(this.clientAddress);
        e.setCreatedOn(this.generated);
        e.setException(this.throwable);
        e.setLevel(this.level);
        e.setMessage(this.message);
        e.setApplication(this.application);
        e.setUserName(this.userName);

        return e;
    }

}
