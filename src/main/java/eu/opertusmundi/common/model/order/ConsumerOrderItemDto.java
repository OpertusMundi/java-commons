package eu.opertusmundi.common.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.account.ProviderDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsumerOrderItemDto extends OrderItemDto {

    @Schema(description = "Asset provider")
    @JsonInclude(Include.NON_NULL)
    private ProviderDto provider;

}
