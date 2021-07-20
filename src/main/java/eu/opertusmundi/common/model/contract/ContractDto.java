package eu.opertusmundi.common.model.contract;

import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractDto {

    @JsonIgnore
    private Integer id;

    @Schema(description = "Unique key")
    private UUID key;

    @Schema(description = "Title")
    @NotEmpty
    private String title;

    @Schema(description = "Version")
    private String version;

}