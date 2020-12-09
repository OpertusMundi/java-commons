package eu.opertusmundi.common.domain;

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

import org.apache.commons.lang3.StringUtils;

import eu.opertusmundi.common.model.asset.AssetFileTypeDto;
import eu.opertusmundi.common.model.profiler.EnumDataProfilerSourceType;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AssetFileType")
@Table(schema = "`file`", name = "`asset_file_type`")
public class AssetFileTypeEntity {

    @Id
    @SequenceGenerator(sequenceName = "`file.asset_file_type_id_seq`", name = "asset_file_type_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "asset_file_type_id_seq", strategy = GenerationType.SEQUENCE)
    @Column(name = "`id`")
    @Getter
    private Integer id;

    @NotNull
    @Column(name = "`category`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumDataProfilerSourceType category;

    @NotNull
    @Column(name = "`format`")
    @Getter
    @Setter
    private String format;

    @Column(name = "`extensions`")
    @Getter
    @Setter
    private String extensions;

    @Column(name = "`allow_bundle`")
    @Getter
    @Setter
    private boolean bundleSupported;

    @Column(name = "`bundle_extensions`")
    @Getter
    @Setter
    private String bundleExtensions;

    @Column(name = "`enabled`")
    @Getter
    @Setter
    private boolean enabled;

    @Column(name = "`notes`")
    @Getter
    @Setter
    private String notes;

    public AssetFileTypeDto toDto() {
        final AssetFileTypeDto t = new AssetFileTypeDto();

        t.setBundleExtensions(StringUtils.split(this.bundleExtensions, ','));
        t.setBundleSupported(this.bundleSupported);
        t.setCategory(this.category);
        t.setExtensions(StringUtils.split(this.extensions, ','));
        t.setFormat(this.format);
        t.setNotes(this.notes);

        return t;
    }

}