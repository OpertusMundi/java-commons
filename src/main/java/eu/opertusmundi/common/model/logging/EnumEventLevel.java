package eu.opertusmundi.common.model.logging;

import java.util.Arrays;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(enumAsRef = true)
public enum EnumEventLevel {
    DEBUG("debug"),
    INFO("notice", "info"),
    WARN("warning", "warn"),
    ERROR("emerg", "panic", "alert", "crit", "err", "error"),
    ;

    @Getter
    private List<String> severity;

    private EnumEventLevel(String... severity) {
        this.severity = Arrays.asList(severity);
    }

    public static EnumEventLevel fromSeverity(String value) {
        for (final EnumEventLevel level : EnumEventLevel.values()) {
            if (level.getSeverity().contains(value.toLowerCase())) {
                return level;
            }
        }
        return EnumEventLevel.ERROR;
    }
}
