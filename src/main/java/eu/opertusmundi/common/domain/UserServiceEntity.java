package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import eu.opertusmundi.common.model.Message;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceStatus;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceType;
import eu.opertusmundi.common.model.asset.service.UserServiceDto;
import eu.opertusmundi.common.model.ingest.ResourceIngestionDataDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "UserService")
@Table(schema = "web", name = "`account_user_service`", uniqueConstraints = {
    @UniqueConstraint(name = "uq_private_service_key", columnNames = {"`key`"}),
    @UniqueConstraint(name = "uq_private_service_pid", columnNames = {"`key`"}),
})
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Getter
@Setter
public class UserServiceEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.account_user_service_id_seq", name = "account_user_service_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_user_service_id_seq", strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PRIVATE)
    private Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "account", nullable = false)
    private AccountEntity account;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID key = UUID.randomUUID();

    @NotNull
    @Column(name = "`title`")
    private String title;

    @Column(name = "`abstract_text`")
    private String abstractText;

    @Column(name = "`geometry`")
    private Geometry geometry;

    @NotNull
    @Column(name = "`version`")
    private String version;

    @NotEmpty
    @Column(name = "`path`")
    private String path;

    @NotEmpty
    @Column(name = "`file_name`")
    private String fileName;

    @NotNull
    @Column(name = "`file_size`")
    private Long fileSize;

    @NotEmpty
    @Column(name = "`crs`")
    private String crs;

    @Column(name = "`encoding`")
    private String encoding;

    @NotEmpty
    @Column(name = "`format`")
    private String format;

    @NotNull
    @Column(name = "`service_type`")
    @Enumerated(EnumType.STRING)
    private EnumUserServiceType serviceType;

    @Column(name = "`automated_metadata`")
    @Type(type = "jsonb")
    private JsonNode automatedMetadata;

    @Column(name = "`ingest_data`")
    @Type(type = "jsonb")
    private ResourceIngestionDataDto ingestData;

    @NotNull
    @Column(name = "`status`", length = 30)
    @Enumerated(EnumType.STRING)
    private EnumUserServiceStatus status = EnumUserServiceStatus.PROCESSING;

    @NotNull
    @Column(name = "`created_on`", nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private ZonedDateTime createdOn = ZonedDateTime.now();

    @NotNull
    @Column(name = "`updated_on`")
    private ZonedDateTime updatedOn;

    @Column(name = "`process_definition`")
    private String processDefinition;

    @Column(name = "`process_instance`")
    private String processInstance;

    @Column(name = "`workflow_error_details`")
    private String workflowErrorDetails;

    @Column(name = "`workflow_error_messages`", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private List<Message> workflowErrorMessages;

    @Column(name = "`helpdesk_error_message`")
    private String helpdeskErrorMessage;

    @Column(name = "`computed_geometry`")
    private boolean computedGeometry = false;

    public UserServiceDto toDto() {
        return this.toDto(false);
    }

    public UserServiceDto toDto(boolean includeHelpdeskDetails) {
        final UserServiceDto a = new UserServiceDto();


        a.setAbstractText(abstractText);
        a.setAutomatedMetadata(automatedMetadata);
        a.setComputedGeometry(computedGeometry);
        a.setCreatedOn(createdOn);
        a.setCrs(crs);
        a.setEncoding(encoding);
        a.setFileName(fileName);
        a.setFileSize(fileSize);
        a.setFormat(format);
        a.setGeometry(geometry);
        a.setHelpdeskErrorMessage(helpdeskErrorMessage);
        a.setId(id);
        a.setIngestData(ingestData);
        a.setKey(key);
        a.setOwner(this.getAccount().toSimpleDto());
        a.setPath(path);
        a.setServiceType(serviceType);
        a.setStatus(status);
        a.setTitle(title);
        a.setUpdatedOn(updatedOn);
        a.setVersion(version);

        if (includeHelpdeskDetails) {
            a.setProcessDefinition(processDefinition);
            a.setProcessInstance(processInstance);
            a.setWorkflowErrorDetails(workflowErrorDetails);
            a.setWorkflowErrorMessages(workflowErrorMessages);
        }

        return a;
    }

}
