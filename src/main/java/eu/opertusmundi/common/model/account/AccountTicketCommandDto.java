package eu.opertusmundi.common.model.account;

import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountTicketCommandDto {

    @JsonIgnore
    private UUID userKey;

    @JsonIgnore
    private UUID messageThreadKey;

    @JsonIgnore
    private String subject;

    @JsonIgnore
    private EnumCustomerType customerType;

    @Schema(description = "Key of the referenced resource e.g. the order key")
    @NotNull
    private UUID resourceKey;

    @Schema(description = "Ticket type used to resolve the resource key")
    @NotNull
    private EnumTicketType type;

    @NotBlank
    private String text;

}
