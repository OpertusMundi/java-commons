package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.message.EnumNotificationType;
import lombok.Getter;

@Entity(name = "NotificationTemplate")
@Table(schema = "messaging", name = "`notification_template`")
public class NotificationTemplateEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @Getter
    private Integer id;

    @NaturalId
    @Column(name = "`type`")
    @Enumerated(EnumType.STRING)
    @Getter
    private EnumNotificationType type;

    @Column(name = "`text`")
    @Getter
    private String text;

    @Column(name = "modified_on")
    @Getter
    private ZonedDateTime modifiedOn;

}
