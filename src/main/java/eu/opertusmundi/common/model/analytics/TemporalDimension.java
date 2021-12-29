package eu.opertusmundi.common.model.analytics;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemporalDimension {

    @Schema(description = "Time interval unit", required = true)
    @NotNull
    private EnumTemporalUnit unit;

    @Schema(description = "Min date in YYYY-MM-DD ISO format")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate min;

    @Schema(description = "Max date in YYYY-MM-DD ISO format")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate max;

}