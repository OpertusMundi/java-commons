package eu.opertusmundi.common.model;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PageRequestDto {

    protected PageRequestDto(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public static PageRequestDto of(int page, int size) {
        return new PageRequestDto(page, size);
    }

    public static PageRequestDto defaultValue() {
        return new PageRequestDto(0, 10);
    }

    @Schema(
        description = "Page index. Page index is 0-based and cannot be a negative number.",
        example = "0",
        required = true
    )
    @NotNull
    protected Integer page;

    @Schema(
        description = "Page size. Page size must be greater than zero.",
        example = "10",
        required = true
    )
    @NotNull
    protected Integer size;

}
