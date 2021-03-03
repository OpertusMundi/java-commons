package eu.opertusmundi.common.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
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
    private String code;

    @Getter
    private EnumLevel level;

    @Getter
    private String description;

    public Message(MessageCode code, String description) {
        this.code        = code.key();
        this.description = description;
        this.level       = EnumLevel.ERROR;
    }

    public Message(MessageCode code, String description, EnumLevel level) {
        this.code        = code.key();
        this.description = description;
        this.level       = level;
    }

    @Override
    public String toString() {
        return this.getDescription();
    }

}
