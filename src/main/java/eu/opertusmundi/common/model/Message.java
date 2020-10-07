package eu.opertusmundi.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public class Message {

    public enum EnumLevel {
        INFO(0),
        WARN(1),
        ERROR(2),
        CRITICAL(3),
        ;

        private final int value;

        private EnumLevel(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static EnumLevel valueOf(int value) {
            for (final EnumLevel r : EnumLevel.values()) {
                if (r.value == value) {
                    return r;
                }
            }
            return null;
        }
    }

    @Getter
    private final String code;

    @Getter
    private final EnumLevel level;

    @Getter
    private final String description;

    public Message(MessageCode code, String description) {
        this.code        = code.key();
        this.description = description;
        this.level       = EnumLevel.ERROR;
    }

    @JsonCreator
    public Message(
        @JsonProperty MessageCode code,
        @JsonProperty String description,
        @JsonProperty EnumLevel level
    ) {
        this.code        = code.key();
        this.description = description;
        this.level       = level;
    }

    @Override
    public String toString() {
        return this.getDescription();
    }

}
