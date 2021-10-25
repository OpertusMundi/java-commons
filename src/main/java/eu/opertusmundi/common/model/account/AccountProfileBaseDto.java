package eu.opertusmundi.common.model.account;

import java.io.Serializable;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountProfileBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "User image. Max allowed image size is `2Mb`")
    @Size(max = 2 * 1024 * 1024)
    protected byte[] image;

    @Schema(description = "User image mime type", example = "image/png")
    protected String imageMimeType;

    @Schema(description = "User locale", defaultValue = "en")
    @Pattern(regexp = "[a-z][a-z]")
    protected String locale;

    @Schema(description = "User phone")
    protected String phone;

}
