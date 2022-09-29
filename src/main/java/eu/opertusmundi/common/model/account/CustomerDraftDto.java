package eu.opertusmundi.common.model.account;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.Message;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CustomerDraftDto {

    @JsonIgnore
    protected UUID bankAccountIdempotentKey;

    protected ZonedDateTime createdAt;

    protected String email;

    @JsonIgnore
    protected Integer id;

    @JsonIgnore
    protected UUID key;

    protected ZonedDateTime modifiedAt;

    protected EnumCustomerRegistrationStatus status;

    protected EnumMangopayUserType type;

    @JsonIgnore
    protected UUID userIdempotentKey;

    @JsonIgnore
    protected UUID walletIdempotentKey;

    @Hidden
    @Schema(description = "Workflow error details")
    @JsonInclude(Include.NON_EMPTY)
    protected String workflowErrorDetails;

    @Hidden
    @ArraySchema(
        arraySchema = @Schema(
            description = "Workflow error messages"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @JsonInclude(Include.NON_EMPTY)
    protected List<Message> workflowErrorMessages;

    @Schema(description = "Helpdesk error message")
    @JsonInclude(Include.NON_EMPTY)
    protected String helpdeskErrorMessage;

}
