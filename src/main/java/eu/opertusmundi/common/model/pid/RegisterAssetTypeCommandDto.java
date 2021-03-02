package eu.opertusmundi.common.model.pid;

import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RegisterAssetTypeCommandDto {

    @NotEmpty
    private String id;

    @NotEmpty
    private String description;

}
