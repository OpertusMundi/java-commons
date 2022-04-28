package eu.opertusmundi.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "code", visible = true,
    defaultImpl = Message.class
)
@JsonSubTypes({
    @Type(name = "BasicMessageCode.Validation", value = ValidationMessage.class),
})
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
        this(code.key(), description, EnumLevel.ERROR);
    }

    public Message(MessageCode code, String description, EnumLevel level) {
        this(code.key(), description, level);
    }
    
    @JsonCreator
    public Message(
        @JsonProperty("code") String code, 
        @JsonProperty("description") String description, 
        @JsonProperty("level") EnumLevel level
    ) {
        this.code        = code;
        this.description = description;
        this.level       = level;
    }

    @Override
    public String toString() {
        return this.getDescription();
    }

}
