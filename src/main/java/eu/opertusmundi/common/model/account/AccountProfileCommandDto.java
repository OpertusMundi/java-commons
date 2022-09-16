package eu.opertusmundi.common.model.account;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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

    @Schema(description = "First name", required = true)
    @NotEmpty
    private String firstName;

    @Schema(description = "Last name", required = true)
    @NotNull
    private String lastName;

    @Schema(description = "Mobile")
    private String mobile;

}