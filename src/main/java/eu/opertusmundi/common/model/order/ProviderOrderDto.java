package eu.opertusmundi.common.model.order;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.account.ConsumerDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProviderOrderDto extends OrderDto{

    @ArraySchema(arraySchema = @Schema(
        description = "Order items. Currently only a single item is allowed per order"),
        minItems = 1,
        maxItems = 1,
        uniqueItems = true,
        schema = @Schema(implementation = ProviderOrderItemDto.class)
    )
    @JsonInclude(Include.NON_EMPTY)
    private List<ProviderOrderItemDto> items = new ArrayList<>();

    @Schema(description = "Order consumer customer")
    @JsonInclude(Include.NON_NULL)
    private ConsumerDto consumer;

    public void addItem(ProviderOrderItemDto i) {
        this.items.add(i);
    }

}
