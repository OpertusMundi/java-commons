package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import eu.opertusmundi.common.model.Message;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.converter.CatalogueItemCommandAttributeConverter;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "ProviderAssetDraft")
@Table(schema = "provider", name = "`asset_draft`", uniqueConstraints = {
    @UniqueConstraint(name = "uq_asset_draft_key", columnNames = {"`key`"}),
})
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class ProviderAssetDraftEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "provider.asset_draft_id_seq", name = "asset_draft_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "asset_draft_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @ManyToOne(targetEntity=AccountEntity.class)
    @JoinColumn(name = "account", nullable = false)
    @Getter
    @Setter
    private AccountEntity account;

    @ManyToOne(targetEntity=AccountEntity.class)
    @JoinColumn(name = "account_vendor", nullable = false)
    @Getter
    @Setter
    private AccountEntity vendorAccount;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Getter
    private final UUID key = UUID.randomUUID();

    @Column(name = "`process_definition`")
    @Getter
    @Setter
    private String processDefinition;

    @Column(name = "`process_instance`")
    @Getter
    @Setter
    private String processInstance;

    @Column(name = "`parent_id`")
    @Getter
    @Setter
    private String parentId;

    @NotNull
    @Column(name = "`title`", nullable = false)
    @Getter
    @Setter
    private String title;

    @NotNull
    @Column(name = "`version`", nullable = false)
    @Getter
    @Setter
    private String version;

    @Column(name = "`type`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private eu.opertusmundi.common.model.catalogue.client.EnumAssetType type;

    @Column(name = "`service_type`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumSpatialDataServiceType serviceType;

    @NotNull
    @Column(name = "`data`")
    @Convert(converter = CatalogueItemCommandAttributeConverter.class)
    @Getter
    @Setter
    private CatalogueItemCommandDto command;

    @NotNull
    @Column(name = "asset_draft", updatable = false, columnDefinition = "uuid")
    @Getter
    @Setter
    private UUID assetDraft;

    @Column(name = "asset_published")
    @Getter
    @Setter
    private String assetPublished;

    @Column(name = "`status`", length = 30)
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumProviderAssetDraftStatus status = EnumProviderAssetDraftStatus.DRAFT;

    @Column(name = "`helpdesk_rejection_reason`")
    @Getter
    @Setter
    private String helpdeskRejectionReason;

    @Column(name = "`provider_rejection_reason`")
    @Getter
    @Setter
    private String providerRejectionReason;

    @Column(name = "`ingested`")
    @Getter
    @Setter
    private boolean ingested;

    @Column(name = "`created_on`", nullable = false)
    @Getter
    private final ZonedDateTime createdOn = ZonedDateTime.now();

    @Column(name = "`modified_on`")
    @Getter
    @Setter
    private ZonedDateTime modifiedOn;

    @Column(name = "`workflow_error_details`")
    @Getter
    @Setter
    private String workflowErrorDetails;

    @Type(type = "jsonb")
    @Column(name = "`workflow_error_messages`", columnDefinition = "jsonb")
    @Getter
    @Setter
    private List<Message> workflowErrorMessages;

    @Column(name = "`helpdesk_error_message`")
    @Getter
    @Setter
    private String helpdeskErrorMessage;

    @Column(name = "`computed_geometry`")
    @Getter
    @Setter
    private boolean computedGeometry;

    public AssetDraftDto toDto() {
        return this.toDto(false);
    }

    public AssetDraftDto toDto(boolean includeHelpdeskDetails) {
        final AssetDraftDto a = new AssetDraftDto();

        a.setAssetDraft(this.assetDraft);
        a.setAssetPublished(this.assetPublished);
        a.setCreatedOn(this.createdOn);
        a.setCommand(this.command);
        a.setComputedGeometry(this.computedGeometry);
        a.setHelpdeskErrorMessage(helpdeskErrorMessage);
        a.setHelpdeskRejectionReason(this.helpdeskRejectionReason);
        a.setId(id);
        a.setIngested(this.ingested);
        a.setKey(this.key);
        a.setModifiedOn(this.modifiedOn);
        a.setOwner(this.getVendorAccount() == null ? this.getAccount().getKey() : this.getVendorAccount().getKey());
        a.setParentId(this.parentId);
        a.setProcessInstance(processInstance);
        a.setProviderRejectionReason(this.providerRejectionReason);
        a.setServiceType(this.serviceType);
        a.setStatus(this.status);
        a.setTitle(this.title);
        a.setType(this.type);
        a.setVersion(this.version);

        a.setPublisher(this.account.getProvider().toProviderDto(true));

        if(includeHelpdeskDetails) {
            a.setWorkflowErrorDetails(workflowErrorDetails);
            a.setWorkflowErrorMessages(workflowErrorMessages);
        }

        return a;
    }

}
