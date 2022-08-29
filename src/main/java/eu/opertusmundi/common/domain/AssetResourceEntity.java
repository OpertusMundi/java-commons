package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.asset.EnumResourceSource;
import eu.opertusmundi.common.model.asset.FileResourceDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AssetResource")
@Table(schema = "`file`", name = "`asset_resource`")
@Getter
@Setter
public class AssetResourceEntity {

    protected AssetResourceEntity() {

    }

    public AssetResourceEntity(UUID draftKey) {
        this.draftKey = draftKey;
    }

    @Id
    @SequenceGenerator(sequenceName = "`file.asset_resource_id_seq`", name = "asset_resource_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "asset_resource_id_seq", strategy = GenerationType.SEQUENCE)
    @Column(name = "`id`")
    @Setter(AccessLevel.PRIVATE)
    private Integer id;

    @NotNull
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @NaturalId
    @Setter(AccessLevel.PRIVATE)
    private String key = UUID.randomUUID().toString();

    @Column(name = "pid")
    private String pid;

    @NotNull
    @Column(name = "draft_key", updatable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID draftKey;

    @NotNull
    @Column(name = "`created_on`")
    private ZonedDateTime createdOn = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`uploaded_by`")
    private AccountEntity account;

    @NotNull
    @Column(name = "`file_name`")
    private String fileName;

    @NotNull
    @Column(name = "`size`")
    private Long size;

    @Column(name = "`category`")
    @Enumerated(EnumType.STRING)
    private EnumAssetType category;

    @NotNull
    @Column(name = "`format`")
    private String format;

    @Column(name = "`encoding`")
    private String encoding;

    @Column(name = "`crs`")
    private String crs;

    @Column(name = "`source`")
    @Enumerated(EnumType.STRING)
    private EnumResourceSource source;

    @Column(name = "`path`")
    private String path;

    public FileResourceDto toDto() {
        return new FileResourceDto(key, null, category, crs, encoding, fileName, format, createdOn, path, size, source);
    }

}