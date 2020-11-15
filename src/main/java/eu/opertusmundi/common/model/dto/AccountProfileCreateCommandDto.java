package eu.opertusmundi.common.model.dto;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountProfileCreateCommandDto extends AccountProfileUpdateCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "User mobile", required = true)
    @NotEmpty
    protected String mobile;

    @Schema(description = "User phone", required = false)
    protected String phone;

}
