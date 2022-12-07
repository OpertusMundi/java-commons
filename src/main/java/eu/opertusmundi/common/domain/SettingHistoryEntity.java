package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.Optional;

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
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.EnumService;
import eu.opertusmundi.common.model.EnumSettingType;
import eu.opertusmundi.common.model.SettingHistoryDto;
import eu.opertusmundi.common.model.account.helpdesk.SimpleHelpdeskAccountDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "SettingHistory")
@Table(
    schema = "`web`", 
    name   = "`setting_history`"
)
@Getter
@Setter
public class SettingHistoryEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.setting_history_id_seq", name = "setting_history_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "setting_history_id_seq", strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PRIVATE)
    private Integer id;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "`service`")
    private EnumService service;

    @NotNull
    @Column(name = "`key`")
    private String key;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "`type`")
    private EnumSettingType type;

    @NotNull
    @Column(name = "`value`")
    private String value;

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
    private ZonedDateTime updatedOn;

    public SettingHistoryDto toDto() {
        final SimpleHelpdeskAccountDto account = this.getUpdatedBy().map(HelpdeskAccountEntity::toSimpleDto).orElse(null);
        return new SettingHistoryDto(id, key, service, type, account, updatedOn, value);
    }
}