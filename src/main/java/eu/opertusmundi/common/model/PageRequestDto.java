package eu.opertusmundi.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PageRequestDto {

    public PageRequestDto(int page, int size) {
        this.page = page;
        this.size = size;
    }

    @Schema(
        description = "Page index. Page index is 0-based and cannot be a negative number.",
        example = "0",
        required = true
    )
    protected int page;

    @Schema(
        description = "Page size. Page size must be greater than zero.",
        example = "10",
        required = true
    )
    protected int size;

}
