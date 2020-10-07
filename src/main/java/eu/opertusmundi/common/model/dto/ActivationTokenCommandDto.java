package eu.opertusmundi.common.model.dto;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "A command used to verify an account or profile email")
public class ActivationTokenCommandDto {

    @JsonIgnore
    @Getter
    @Setter
    private Integer userId;

    @Schema(
        description = "Email address to verify",
        required = true
    )
    @NotEmpty
    @Getter
    @Setter
    private String email;

}
