package eu.opertusmundi.common.model.contract.provider;

import java.io.Serializable;

import eu.opertusmundi.common.model.contract.EnumContractStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public class ProviderTemplateContractHistoryDto extends ProviderTemplateContractDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Status")
    private EnumContractStatus status;

}
