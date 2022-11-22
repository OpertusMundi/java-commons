package eu.opertusmundi.common.model.message;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactFormDto {

    @JsonIgnore
    private Integer id;

    @JsonInclude(Include.NON_EMPTY)
    @Schema(hidden = true)
    private String processDefinition;

    @JsonInclude(Include.NON_EMPTY)
    @Schema(hidden = true)
    private String processInstance;

    private UUID                  key;
    private String                companyName;
    private String                firstName;
    private String                lastName;
    private String                email;
    private String                phoneCountryCode;
    private String                phoneNumber;
    private String                message;
    private ZonedDateTime         createdAt;
    private ZonedDateTime         updatedAt;
    private EnumContactFormStatus status;
    private boolean               privacyTermsAccepted;

}
