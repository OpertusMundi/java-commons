package eu.opertusmundi.common.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public class SettingUpdateDto {
    
    @NotEmpty
    private String key;

    @NotNull
    private EnumService service;

    private String value;

}
