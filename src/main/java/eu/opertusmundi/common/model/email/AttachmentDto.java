package eu.opertusmundi.common.model.email;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AttachmentDto {

    @Schema(description = "The attachment filename", required = true)
    private final String name;

    @Schema(description = "The data", required = true)
    private final byte[] data;

    @Schema(description = "The MIME type", required = true)
    private final String type;

    @Builder
    public AttachmentDto(String name, byte[] data, String type) {
        this.name = name;
        this.data = data;
        this.type = type;
    }

}
