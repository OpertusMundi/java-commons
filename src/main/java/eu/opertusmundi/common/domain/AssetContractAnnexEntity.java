package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
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

import eu.opertusmundi.common.model.asset.AssetContractAnnexDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AssetContractAnnex")
@Table(schema = "`file`", name = "`asset_contract_annex`")
public class AssetContractAnnexEntity {

    protected AssetContractAnnexEntity() {

    }

    public AssetContractAnnexEntity(UUID draftKey) {
        this.draftKey = draftKey;
    }

    @Id
    @SequenceGenerator(sequenceName = "`file.asset_contract_annex_id_seq`", name = "asset_contract_annex_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "asset_contract_annex_id_seq", strategy = GenerationType.SEQUENCE)
    @Column(name = "`id`")
    @Getter
    private Integer id;

    @NotNull
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @NaturalId
    @Getter
    private final String key = UUID.randomUUID().toString();

    @Column(name = "pid")
    @Getter
    @Setter
    private String pid;

    @NotNull
    @Column(name = "draft_key", updatable = false, columnDefinition = "uuid")
    @Getter
    private UUID draftKey;

    @NotNull
    @Column(name = "`created_on`")
    @Getter
    @Setter
    private ZonedDateTime createdOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`uploaded_by`")
    @Getter
    @Setter
    private AccountEntity account;

    @NotNull
    @Column(name = "`file_name`")
    @Getter
    @Setter
    private String fileName;

    @Column(name = "`size`")
    @Getter
    @Setter
    private Long size;

    @Column(name = "`description`")
    @Getter
    @Setter
    private String description;

    public AssetContractAnnexDto toDto() {
        return new AssetContractAnnexDto(this.key, this.fileName, this.size, this.description, this.createdOn);
    }

}