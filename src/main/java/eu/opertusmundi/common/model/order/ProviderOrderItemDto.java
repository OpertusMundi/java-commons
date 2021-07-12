package eu.opertusmundi.common.model.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProviderOrderItemDto extends OrderItemDto {

    @Schema(description = "Asset contract template identifier")
    private Integer contractTemplateId;

    @Schema(description = "Asset contract template version")
    private String contractTemplateVersion;

}
