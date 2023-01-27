package eu.opertusmundi.common.model;

import java.time.ZonedDateTime;

import eu.opertusmundi.common.model.account.helpdesk.SimpleHelpdeskAccountDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "of")
@Getter
public class SettingDto {

    protected String                   key;
    protected EnumService              service;
    protected EnumSettingType          type;
    protected SimpleHelpdeskAccountDto updatedBy;
    protected ZonedDateTime            updatedOn;
    protected String                   value;

    public boolean asBoolean() {
        if (this.type != EnumSettingType.BOOLEAN) {
            return false;
        }
        return Boolean.parseBoolean(this.value);
    }

    public Integer asInteger() {
        if (this.type != EnumSettingType.NUMERIC) {
            return null;
        }
        return Integer.parseInt(this.value);
    }

}
