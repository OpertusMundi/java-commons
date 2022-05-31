package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.file.FileCopyResourceDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "FileCopyResource")
@Table(schema = "`file`", name = "`file_copy_resource`")
public class FileCopyResourceEntity {

    @Id
    @SequenceGenerator(sequenceName = "`file.file_copy_resource_id_seq`", name = "file_copy_resource_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "file_copy_resource_id_seq", strategy = GenerationType.SEQUENCE)
    @Column(name = "`id`")
    @Getter
    private Integer id;

    @NotNull
    @Column(name = "idempotent_key", updatable = false, columnDefinition = "uuid")
    @NaturalId
    @Getter
    private final UUID idempotentKey = UUID.randomUUID();

    @NotNull
    @Column(name = "account_key", updatable = false, columnDefinition = "uuid")
    @Getter
    @Setter
    private UUID accountKey;

    @NotEmpty
    @Column(name = "`asset_pid`")
    @Getter
    @Setter
    private String assetPid;

    @NotEmpty
    @Column(name = "`resource_key`")
    @Getter
    @Setter
    private String resourceKey;

    @NotNull
    @Column(name = "`created_on`")
    @Getter
    private final ZonedDateTime createdOn = ZonedDateTime.now();

    @Column(name = "`completed_on`")
    @Getter
    @Setter
    private ZonedDateTime completedOn;

    @NotEmpty
    @Column(name = "`source_path`")
    @Getter
    @Setter
    private String sourcePath;

    @NotEmpty
    @Column(name = "`target_path`")
    @Getter
    @Setter
    private String targetPath;

    @NotNull
    @Column(name = "`size`")
    @Getter
    @Setter
    private Long size;

    @Column(name = "`error_message`")
    @Getter
    @Setter
    private String errorMessage;

    public FileCopyResourceDto toDto() {
        final FileCopyResourceDto f = new FileCopyResourceDto();

        f.setAccountKey(accountKey);
        f.setAssetPid(assetPid);
        f.setCompletedOn(completedOn);
        f.setCreatedOn(createdOn);
        f.setErrorMessage(errorMessage);
        f.setId(id);
        f.setIdempotentKey(idempotentKey);
        f.setResourceKey(resourceKey);
        f.setSize(size);
        f.setSourcePath(sourcePath);
        f.setTargetPath(targetPath);

        return f;
    }

}