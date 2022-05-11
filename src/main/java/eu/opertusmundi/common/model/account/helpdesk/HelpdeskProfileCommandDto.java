package eu.opertusmundi.common.model.account.helpdesk;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class HelpdeskProfileCommandDto {

    @NotEmpty
    private String firstName;

    @JsonIgnore
    private Integer id;

    private byte[] image;

    private String imageMimeType;

    @NotEmpty
    private String lastName;

    @NotEmpty
    private String locale;

    private String mobile;

    private String phone;

}
