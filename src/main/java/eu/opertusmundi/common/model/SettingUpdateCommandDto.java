package eu.opertusmundi.common.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor(staticName = "of")
@Getter
public class SettingUpdateCommandDto {

    @JsonIgnore
    @Setter
    private Integer userId;
    
    @Valid
    @NotEmpty
    private List<SettingUpdateDto> updates;

}
