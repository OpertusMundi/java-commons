package eu.opertusmundi.common.model.message.client;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.message.EnumMessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientBaseMessageDto {

    public ClientBaseMessageDto(EnumMessageType type) {
        this.type = type;
    }

    @JsonIgnore
    @Getter(AccessLevel.PRIVATE)
    private final EnumMessageType type;

    @Schema(description = "Message unique id")
    private UUID id;

    @Schema(description = "Message text")
    @NotEmpty
    private String text;

    @Schema(description = "Created at")
    private ZonedDateTime createdAt;

    @Schema(description = "Read at")
    private ZonedDateTime readAt;

    @Schema(description = "Message is marked as read")
    private boolean read;

    @Schema(description = "Message sender identifier")
    private UUID senderId;

    @Schema(description = "Message sender contact")
    @JsonInclude(Include.NON_NULL)
    private ClientContactDto sender;

}
