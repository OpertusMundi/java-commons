package eu.opertusmundi.common.model.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountProfileBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "User image")
    private byte[] image;

    @Schema(description = "User image mime type", example = "image/png")
    private String imageMimeType;

    private String mobile;
    private String phone;

}
