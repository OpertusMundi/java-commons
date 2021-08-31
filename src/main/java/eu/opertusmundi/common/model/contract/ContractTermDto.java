package eu.opertusmundi.common.model.contract;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public class ContractTermDto {

    @Schema(description = "Icon name")
    private EnumIcon icon;

    @Schema(description = "Icon category")
    private EnumIconCategory category;

    @Schema(description = "Base64 encoded icon SVG image")
    private byte[] image;

    @Schema(description = "Description of the icon")
    private String description;

}