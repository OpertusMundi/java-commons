package eu.opertusmundi.common.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.account.CustomerProfessionalDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HelpdeskOrderItemDto extends OrderItemDto {

    @Schema(description = "Asset contract template identifier")
    @JsonInclude(Include.NON_NULL)
    private Integer contractTemplateId;

    @Schema(description = "Asset contract template version")
    @JsonInclude(Include.NON_NULL)
    private String contractTemplateVersion;

    @Schema(description = "Asset provider customer")
    @JsonInclude(Include.NON_NULL)
    private CustomerProfessionalDto provider;

}
