package eu.opertusmundi.common.model.dto;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountProfileCommandDto extends AccountProfileBaseDto implements Serializable {

    @Builder
    public AccountProfileCommandDto(
        byte[] image, String imageMimeType, String locale, String phone, Integer id, @NotEmpty String firstName,
        @NotEmpty String lastName, @NotEmpty String mobile
    ) {
        super(image, imageMimeType, locale, phone);

        this.id        = id;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.mobile    = mobile;
    }

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Integer id;

    @Schema(description = "User first name", required = true)
    @NotEmpty
    private String firstName;

    @Schema(description = "User last name", required = true)
    @NotEmpty
    private String lastName;

    @Schema(description = "User mobile", required = true)
    private String mobile;

}