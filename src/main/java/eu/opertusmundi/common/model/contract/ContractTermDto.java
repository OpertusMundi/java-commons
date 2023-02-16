package eu.opertusmundi.common.model.contract;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
public class ContractTermDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Icon name")
    private EnumIcon icon;

    @Schema(description = "Icon category")
    private EnumIconCategory category;

    @Schema(description = "Base64 encoded icon SVG image")
    private byte[] image;

    @Schema(description = "Description of the icon. Property to be deprecated in future release. Use `text` property instead", deprecated = true)
    private String description;

    @Schema(description = "Multiline description of the section. This array is composed from the short description "
                        + "of the selected option and sub-options. The values may contain HTML elements i.e. `<p>Text</p>`. "
                        + "Replaces property `description`")
    private List<String> text;

}
