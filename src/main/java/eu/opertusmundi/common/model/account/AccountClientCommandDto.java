package eu.opertusmundi.common.model.account;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountClientCommandDto {

    @JsonIgnore
    private Integer accountId;

    @Schema(description = "User-defined client name")
    @NotEmpty
    private String alias;

}
