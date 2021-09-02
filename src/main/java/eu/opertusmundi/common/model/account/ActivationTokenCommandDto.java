package eu.opertusmundi.common.model.account;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Schema(description = "A command used to verify an account or profile email")
public class ActivationTokenCommandDto {

    @Schema(description = "Email address to verify", required = true)
    @NotEmpty
    @Getter
    @Setter
    private String email;

    @JsonIgnore
    @Getter
    @Setter
    private int duration;

}
