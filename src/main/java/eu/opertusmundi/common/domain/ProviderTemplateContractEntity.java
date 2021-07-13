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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.contract.ProviderTemplateContractDto;
import lombok.Getter;


@Entity(name = "ProviderContract")
@Table(
    schema = "contract", name = "`provider_contract`"
)
public class ProviderTemplateContractEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(
        sequenceName = "contract.provider_contract_id_seq", name = "provider_contract_id_seq", allocationSize = 1
    )
    @GeneratedValue(generator = "provider_contract_id_seq", strategy = GenerationType.SEQUENCE)
    @lombok.Setter()
    @lombok.Getter()
    Integer id ;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Getter
    private final UUID key = UUID.randomUUID();

    @Column(name = "`provider_key`")
    @lombok.Getter
    @lombok.Setter
    UUID providerKey;

    @Column(name = "`parent_id`")
    @lombok.Getter
    @lombok.Setter
    Integer parentId;

    @Column(name = "`master_contract_id`")
    @lombok.Getter
    @lombok.Setter
    Integer masterContractId;

    @Column(name = "`master_contract_version`")
    @lombok.Getter
    @lombok.Setter
    String masterContractVersion;


    @Column(name = "`title`")
    @lombok.Getter()
    @lombok.Setter()
    String title;

    @Column(name = "`subtitle`")
    @lombok.Getter
    @lombok.Setter
    String subtitle;

    @Column(name = "`version`")
    @lombok.Getter
    @lombok.Setter
    String version;

    @Column(name = "`active`")
    @lombok.Getter()
    @lombok.Setter()
    Boolean active;


    @Column(name = "`created_at`")
    @lombok.Getter
    @lombok.Setter
    ZonedDateTime createdAt;


    @Column(name = "`modified_at`")
    @lombok.Getter
    @lombok.Setter
    ZonedDateTime modifiedAt;

    public ProviderTemplateContractDto toDto() {
    	final ProviderTemplateContractDto c = new ProviderTemplateContractDto();

        c.setId(this.id);
        c.setKey(key);
        c.setProviderKey(this.providerKey);
        c.setParentId(this.parentId);
        c.setMasterContractId(masterContractId);
        c.setMasterContractVersion(masterContractVersion);
        c.setTitle(this.title);
        c.setSubtitle(this.subtitle);
        c.setCreatedAt(this.createdAt);
        c.setModifiedAt(this.modifiedAt);
        c.setVersion(this.version);


        return c;
    }

}
