package eu.opertusmundi.common.model;

import java.time.ZonedDateTime;

import eu.opertusmundi.common.model.account.helpdesk.SimpleHelpdeskAccountDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class SettingHistoryDto extends SettingDto {

    public SettingHistoryDto(
        Integer id, String key, EnumService service, EnumSettingType type, SimpleHelpdeskAccountDto updatedBy, ZonedDateTime updatedOn, String value
    ) {
        super();
        this.id        = id;
        this.key       = key;
        this.service   = service;
        this.type      = type;
        this.updatedBy = updatedBy;
        this.updatedOn = updatedOn;
        this.value     = value;
    }

    private Integer id;

}
