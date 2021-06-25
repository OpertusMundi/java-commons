package eu.opertusmundi.common.model.spatial;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LanguageDto {

    @Schema(description = "The language ISO 639-1 two-letter code")
    private String code;

    private String name;

    @JsonIgnore
    private boolean active;
}
