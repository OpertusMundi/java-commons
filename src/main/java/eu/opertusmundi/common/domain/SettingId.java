package eu.opertusmundi.common.domain;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.EnumService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SettingId implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private EnumService service;

    @NotEmpty
    private String key;

}