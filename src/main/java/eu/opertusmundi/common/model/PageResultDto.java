package eu.opertusmundi.common.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class PageResultDto<Item> {

    protected PageResultDto(PageRequestDto pageRequest) {
        this.count       = 0;
        this.items       = new ArrayList<>();
        this.pageRequest = pageRequest;
    }

    protected PageResultDto(PageRequestDto pageRequest, long count, List<Item> items) {
        this.count       = count;
        this.items       = items;
        this.pageRequest = pageRequest;
    }

    @Schema(
        description = "Page request options."
    )
    private final PageRequestDto pageRequest;

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

    public <NewType> PageResultDto<NewType> convert(Function<Item, NewType> converter) {
        final List<NewType> items = this.items.stream().map(converter).collect(Collectors.toList());

        return new PageResultDto<>(this.pageRequest, this.count, items);
    }

    public static <EntityType, DtoType> PageResultDto<DtoType> from(Page<EntityType> page, Function<EntityType, DtoType> transform) {
        final List<DtoType> items = page.getContent().stream().map(transform).collect(Collectors.toList());

        final PageRequestDto pageRequest = new PageRequestDto(page.getNumber(), page.getSize());

        return new PageResultDto<>(pageRequest, page.getTotalElements(), items);
    }

    public static <Item> PageResultDto<Item> empty(PageRequestDto pageRequest) {
        return new PageResultDto<Item>(pageRequest);
    }

    public static <R> PageResultDto<R> of(int page, int size, List<R> items) {
        return PageResultDto.of(page, size, items, items.size());
    }

    public static <R> PageResultDto<R> of(int page, int size, List<R> items, long count) {
        final PageRequestDto   p = new PageRequestDto(page, size);

        return new PageResultDto<R>(p, count, items);
    }

}
