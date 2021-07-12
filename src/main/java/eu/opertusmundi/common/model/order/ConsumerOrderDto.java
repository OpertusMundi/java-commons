package eu.opertusmundi.common.model.order;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsumerOrderDto extends OrderDto {

    @ArraySchema(arraySchema = @Schema(
        description = "Order items. Currently only a single item is allowed per order"),
        minItems = 1,
        maxItems = 1,
        uniqueItems = true,
        schema = @Schema(implementation = ConsumerOrderItemDto.class)
    )
    @JsonInclude(Include.NON_EMPTY)
    private List<ConsumerOrderItemDto> items = new ArrayList<>();

    public void addItem(ConsumerOrderItemDto i) {
        this.items.add(i);
    }

}
