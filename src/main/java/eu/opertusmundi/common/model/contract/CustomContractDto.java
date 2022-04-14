package eu.opertusmundi.common.model.contract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.asset.AssetContractAnnexDto;
import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CustomContractDto extends ContractDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public CustomContractDto() {
        this.type    = EnumContractType.UPLOADED_CONTRACT;
        this.annexes = new ArrayList<>();
    }

    public CustomContractDto(List<AssetContractAnnexDto> annexes) {
        this.type    = EnumContractType.UPLOADED_CONTRACT;
        this.annexes = annexes;
    }

    @Schema(description = "Uploaded file name")
    @JsonInclude(Include.NON_EMPTY)
    @Setter
    private String fileName;

    @Schema(description = "Uploaded file size")
    @JsonInclude(Include.NON_NULL)
    @Setter
    private Long fileSize;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Contract annexes"
        ),
        minItems = 0
    )
    private final List<AssetContractAnnexDto> annexes;

}
