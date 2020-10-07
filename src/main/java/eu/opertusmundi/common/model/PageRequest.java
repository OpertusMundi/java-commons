package eu.opertusmundi.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PageRequest {

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
