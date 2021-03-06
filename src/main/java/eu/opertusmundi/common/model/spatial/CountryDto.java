package eu.opertusmundi.common.model.spatial;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CountryDto {

    @Schema(description = "The country ISO 3166-1 alpha-2 code")
    private String code;

    private String name;

}
