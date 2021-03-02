package eu.opertusmundi.common.model.pid;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDto {

    private String name;

    @NotEmpty
    @JsonProperty("user_namespace")
    private String userNamespace;

    private Integer id;

}
