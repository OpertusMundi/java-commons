package eu.opertusmundi.common.model.dto;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountProfileUpdateCommandDto extends AccountProfileBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    protected Integer id;

    @Schema(description = "User first name", required = true)
    @NotEmpty
    protected String firstName;

    @Schema(description = "User image")
    private byte[] image;

    @Schema(description = "User image mime type", example = "image/png")
    private String imageMimeType;

    @Schema(description = "User last name", required = true)
    @NotEmpty
    protected String lastName;

    @Schema(description = "User locale", required = false, defaultValue = "en")
    @Pattern(regexp = "[a-z][a-z]")
    protected String locale;

}
