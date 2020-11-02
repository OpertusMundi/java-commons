package eu.opertusmundi.common.model.dto;

import java.io.Serializable;
import java.util.List;

import javax.validation.Valid;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountProfileCreateCommandDto extends AccountProfileUpdateCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ArraySchema(
        arraySchema = @Schema(
            description = "User addreses"
        ),
        minItems = 0
    )
    @Valid
    private List<AddressCommandDto> addresses;

}
