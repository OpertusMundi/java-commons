package eu.opertusmundi.common.model.pid;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class RegisterUserCommandDto {

    @NotEmpty
    private String name;

    @NotEmpty
    @JsonProperty("user_namespace")
    private String userNamespace;

}
