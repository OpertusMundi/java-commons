package eu.opertusmundi.common.model;

import java.time.ZonedDateTime;

import eu.opertusmundi.common.model.account.helpdesk.SimpleHelpdeskAccountDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName = "of")
@Getter
public class SettingDto {

    private String                   key;
    private EnumService              service;
    private EnumSettingType          type;
    private SimpleHelpdeskAccountDto updatedBy;
    private ZonedDateTime            updatedOn;
    private String                   value;

    public boolean asBoolean() {
        if (this.type != EnumSettingType.BOOLEAN) {
            return false;
        }
        return Boolean.parseBoolean(this.value);
    }

}
