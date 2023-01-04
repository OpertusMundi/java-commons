package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.EnumService;
import eu.opertusmundi.common.model.EnumSettingType;
import eu.opertusmundi.common.model.SettingDto;
import eu.opertusmundi.common.model.account.helpdesk.SimpleHelpdeskAccountDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Setting")
@Table(
    schema = "`web`",
    name   = "`settings`"
)
@IdClass(SettingId.class)
public class SettingEntity {

    @Id
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "`service`")
    @Getter
    @Setter
    private EnumService service;

    @Id
    @NotNull
    @Column(name = "`key`")
    @Getter
    @Setter
    private String key;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "`type`")
    @Getter
    @Setter
    private EnumSettingType type;

    @NotNull
    @Column(name = "`value`")
    @Getter
    @Setter
    private String value;

    @NotNull
    @Column(name = "`read_only`")
    @Getter
    @Setter
    private boolean readOnly;

    @ManyToOne()
    @JoinColumn(name = "updated_by")
    private HelpdeskAccountEntity updatedBy;

    public Optional<HelpdeskAccountEntity> getUpdatedBy() {
        return Optional.ofNullable(this.updatedBy);
    }

    public void setUpdatedBy(HelpdeskAccountEntity updatedBy) {
        this.updatedBy = updatedBy;
    }

    @NotNull
    @Column(name = "`updated_on`")
    @Getter
    @Setter
    private ZonedDateTime updatedOn;

    public SettingDto toDto() {
        final SimpleHelpdeskAccountDto account = this.getUpdatedBy().map(HelpdeskAccountEntity::toSimpleDto).orElse(null);

        return SettingDto.of(key, service, type, account, updatedOn, value);
    }
}