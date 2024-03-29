package eu.opertusmundi.common.model.contract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateContractDto extends ContractDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public TemplateContractDto() {
        this.type = EnumContractType.MASTER_CONTRACT;
    }

    @JsonIgnore
    private Integer id;

    @Schema(description = "Unique key")
    private UUID key;

    @Schema(description = "Title")
    @NotEmpty
    private String title;

    @Schema(description = "Version")
    private String version;

    @ArraySchema(
        arraySchema = @Schema(
            description = "License terms"
        ),
        minItems = 0
    )
    private List<ContractTermDto> terms = new ArrayList<>();

}
