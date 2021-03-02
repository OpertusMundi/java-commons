package eu.opertusmundi.common.model.pid;

import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetTypeRegistrationDto {

    @NotEmpty
    private String id;

    @NotEmpty
    private String description;

}