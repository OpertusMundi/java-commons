package eu.opertusmundi.common.model.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor(staticName = "of")
@Getter
@Setter
@JsonIgnoreType
public class AccountCredentialsDto {

    @JsonIgnore
    private String username;

    @JsonIgnore
    private String password;

}
