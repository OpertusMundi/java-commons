package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.email.EnumMailType;
import lombok.Getter;

@Entity(name = "MailTemplate")
@Table(schema = "messaging", name = "`mail_template`")
public class MailTemplateEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @Getter
    private Integer id;

    @NaturalId
    @Column(name = "`type`")
    @Enumerated(EnumType.STRING)
    @Getter
    private EnumMailType type;

    @Column(name = "`subject_template`")
    @Getter
    private String subjectTemplate;

    @Column(name = "`content_template`")
    @Getter
    private String contentTemplate;

    @Column(name = "`sender_name`")
    @Getter
    private String senderName;

    @Column(name = "`sender_email`")
    @Getter
    private String senderEmail;

    @Column(name = "modified_on")
    @Getter
    private ZonedDateTime modifiedOn;

}
