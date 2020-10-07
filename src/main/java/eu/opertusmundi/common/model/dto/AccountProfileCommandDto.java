package eu.opertusmundi.common.model.dto;

import java.io.Serializable;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountProfileCommandDto extends AccountProfileBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Integer id;

    @Valid
    private List<AddressCommandDto> addresses;

    @Schema(description = "User first name", required = true)
    @NotEmpty
    private String firstName;

    @Schema(description = "User last name", required = true)
    @NotEmpty
    private String lastName;

}
