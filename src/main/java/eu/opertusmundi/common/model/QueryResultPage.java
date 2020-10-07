package eu.opertusmundi.common.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class QueryResultPage<Item> {

    public QueryResultPage(PageRequest pageRequest) {
        this.count       = 0;
        this.items       = new ArrayList<>();
        this.pageRequest = pageRequest;
    }

    @Schema(
        description = "Page request options."
    )
    private final PageRequest pageRequest;

    @Schema(
        description = "Total number of items",
        example = "100"
    )
    private final long count;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Items for the current page"
        ),
        minItems = 0
    )
    private final List<Item> items;

    public <NewType> QueryResultPage<NewType> convert(Function<Item, NewType> converter) {
        final List<NewType> items = this.items.stream().map(converter).collect(Collectors.toList());

        return new QueryResultPage<>(this.pageRequest, this.count, items);
    }

    public static <EntityType, DtoType> QueryResultPage<DtoType> from(Page<EntityType> page, Function<EntityType, DtoType> transform) {
        final List<DtoType> items = page.getContent().stream().map(transform).collect(Collectors.toList());

        final PageRequest pageRequest = new PageRequest(page.getNumber(), page.getSize());

        return new QueryResultPage<>(pageRequest, page.getTotalElements(), items);
    }

    public static <Item> QueryResultPage<Item> empty(PageRequest pageRequest) {
        return new QueryResultPage<Item>(pageRequest);
    }

}
